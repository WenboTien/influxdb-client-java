/**
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.influxdata.flux.impl

import java.io.IOException
import java.util.function.BiConsumer
import java.util.logging.{Level, Logger}

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import javax.annotation.Nonnull
import org.influxdata.flux.FluxClientScala
import org.influxdata.flux.domain.{FluxRecord, FluxTable}
import org.influxdata.flux.option.FluxConnectionOptions
import org.influxdata.platform.Arguments
import org.influxdata.platform.error.InfluxException
import org.influxdata.platform.rest.{AbstractQueryClient, Cancellable, LogLevel}

import scala.compat.java8.FunctionConverters.asJavaConsumer

/**
 * @author Jakub Bednar (bednar@github) (06/11/2018 08:19)
 */
class FluxClientScalaImpl(@Nonnull options: FluxConnectionOptions,
                          @Nonnull val bufferSize: Int,
                          @Nonnull val overflowStrategy: OverflowStrategy)

  extends AbstractFluxClient(options.getOkHttpClient, options.getUrl, options.getParameters, classOf[FluxService])
    with FluxClientScala {

  Arguments.checkNotNull(overflowStrategy, "overflowStrategy")
  Arguments.checkNotNull(bufferSize, "bufferSize")

  private val LOG = Logger.getLogger(classOf[FluxClientScalaImpl].getName)

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream [[FluxRecord]]s to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the stream of [[FluxRecord]]s
   */
  override def query(query: String): Source[FluxRecord, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")

    Source
      .single(query)
      .map(it => fluxService.query(createBody(AbstractQueryClient.DEFAULT_DIALECT.toString, it)))
      .flatMapConcat(queryCall => {
        Source.queue[FluxRecord](bufferSize, overflowStrategy)
          .mapMaterializedValue(queue => {

            val eventualDone = queue.watchCompletion()
            val consumer = new FluxCsvParser.FluxResponseConsumer() {

              override
              def accept(index: Int, @Nonnull cancellable: Cancellable, @Nonnull table: FluxTable): Unit = {
              }

              override
              def accept(index: Int, @Nonnull cancellable: Cancellable, @Nonnull record: FluxRecord): Unit = {
                if (eventualDone.isCompleted) {
                  cancellable.cancel()
                } else {
                  queue.offer(record)
                }
              }
            }

            val onError = asJavaConsumer[Throwable](t =>  queue.fail(t))

            this.query(queryCall, consumer, onError, () => queue.complete, true)
          })
    })
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream measurements to [[Stream]].
   *
   * @param query           the flux query to execute
   * @param measurementType the measurement (POJO)
   * @tparam M the type of the measurement (POJO)
   * @return the stream of measurements
   */
  override def query[M](query: String, measurementType: Class[M]): Source[M, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")
    Arguments.checkNotNull(measurementType, "measurementType")

    this.query(query).map(t => resultMapper.toPOJO(t, measurementType))
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query the flux query to execute
   * @return the response stream
   */
  override def queryRaw(query: String): Source[String, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")

    queryRaw(query, AbstractQueryClient.DEFAULT_DIALECT.toString)
  }

  /**
   * Executes the Flux query against the InfluxDB and asynchronously stream response to [[Stream]].
   *
   * @param query   the flux query to execute
   * @param dialect Dialect is an object defining the options to use when encoding the response.
   *                [[http://bit.ly/flux-dialect See dialect SPEC]].
   * @return the response stream
   */
  override def queryRaw(query: String, dialect: String): Source[String, NotUsed] = {

    Arguments.checkNonEmpty(query, "query")

    Source
      .single(query)
      .map(it => fluxService.query(createBody(dialect, it)))
      .flatMapConcat(queryCall => {
        Source.queue[String](bufferSize, overflowStrategy)
          .mapMaterializedValue(queue => {

            val eventualDone = queue.watchCompletion()

            val onResponse = new BiConsumer[Cancellable, String] {
              override def accept(cancellable: Cancellable, line: String): Unit = {
                if (eventualDone.isCompleted) {
                  cancellable.cancel()
                } else {
                  queue.offer(line)
                }
              }
            }

            val onError = asJavaConsumer[Throwable](t =>  queue.fail(t))

            this.queryRaw(queryCall, onResponse, onError, () => queue.complete, true)
          })
      })
  }

  /**
   * Check the status of InfluxDB Server.
   *
   * @return `true` if server is healthy otherwise return `false`
   */
  override def ping: Boolean = {
    try {
      fluxService.ping().execute().isSuccessful
    } catch {
      case e: IOException =>
        LOG.log(Level.WARNING, "Ping request wasn't successful", e)
        false
    }
  }

  /**
   * Returns the version of the connected InfluxDB Server.
   *
   * @return the version String, otherwise unknown.
   */
  override def version: String = {
    try {
      val response = fluxService.ping().execute()
      getVersion(response)
    } catch {
      case e: IOException => throw new InfluxException(e)
    }
  }

  /**
   * Gets the [[LogLevel]] that is used for logging requests and responses.
   *
   * @return the [[LogLevel]] that is used for logging requests and responses
   */
  override def getLogLevel: LogLevel = {
    super.getLogLevel(loggingInterceptor)
  }

  /**
   * Sets the log level for the request and response information.
   *
   * @param logLevel the log level to set.
   * @return the FluxClient instance to be able to use it in a fluent manner.
   */
  override def setLogLevel(logLevel: LogLevel): FluxClientScala = {

    Arguments.checkNotNull(logLevel, "LogLevel")

    super.setLogLevel(loggingInterceptor, logLevel)
    this
  }
}
