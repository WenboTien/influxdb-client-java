/*
 * Influx API Service
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.influxdata.client.domain;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.influxdata.client.domain.TelegrafRequestAgent;
import org.influxdata.client.domain.TelegrafRequestPlugin;

/**
 * TelegrafRequest
 */

public class TelegrafRequest {
  public static final String SERIALIZED_NAME_NAME = "name";
  @SerializedName(SERIALIZED_NAME_NAME)
  private String name;

  public static final String SERIALIZED_NAME_DESCRIPTION = "description";
  @SerializedName(SERIALIZED_NAME_DESCRIPTION)
  private String description;

  public static final String SERIALIZED_NAME_AGENT = "agent";
  @SerializedName(SERIALIZED_NAME_AGENT)
  private TelegrafRequestAgent agent = null;

  public static final String SERIALIZED_NAME_PLUGINS = "plugins";
  @SerializedName(SERIALIZED_NAME_PLUGINS)
  @JsonAdapter(TelegrafRequestPluginsAdapter.class)
  private List<TelegrafRequestPlugin> plugins = new ArrayList<>();

  public static final String SERIALIZED_NAME_ORG_I_D = "orgID";
  @SerializedName(SERIALIZED_NAME_ORG_I_D)
  private String orgID;

  public TelegrafRequest name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TelegrafRequest description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public TelegrafRequest agent(TelegrafRequestAgent agent) {
    this.agent = agent;
    return this;
  }

   /**
   * Get agent
   * @return agent
  **/
  @ApiModelProperty(value = "")
  public TelegrafRequestAgent getAgent() {
    return agent;
  }

  public void setAgent(TelegrafRequestAgent agent) {
    this.agent = agent;
  }

  public TelegrafRequest plugins(List<TelegrafRequestPlugin> plugins) {
    this.plugins = plugins;
    return this;
  }

  public TelegrafRequest addPluginsItem(TelegrafRequestPlugin pluginsItem) {
    if (this.plugins == null) {
      this.plugins = new ArrayList<>();
    }
    this.plugins.add(pluginsItem);
    return this;
  }

   /**
   * Get plugins
   * @return plugins
  **/
  @ApiModelProperty(value = "")
  public List<TelegrafRequestPlugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(List<TelegrafRequestPlugin> plugins) {
    this.plugins = plugins;
  }

  public TelegrafRequest orgID(String orgID) {
    this.orgID = orgID;
    return this;
  }

   /**
   * Get orgID
   * @return orgID
  **/
  @ApiModelProperty(value = "")
  public String getOrgID() {
    return orgID;
  }

  public void setOrgID(String orgID) {
    this.orgID = orgID;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TelegrafRequest telegrafRequest = (TelegrafRequest) o;
    return Objects.equals(this.name, telegrafRequest.name) &&
        Objects.equals(this.description, telegrafRequest.description) &&
        Objects.equals(this.agent, telegrafRequest.agent) &&
        Objects.equals(this.plugins, telegrafRequest.plugins) &&
        Objects.equals(this.orgID, telegrafRequest.orgID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, agent, plugins, orgID);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TelegrafRequest {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    agent: ").append(toIndentedString(agent)).append("\n");
    sb.append("    plugins: ").append(toIndentedString(plugins)).append("\n");
    sb.append("    orgID: ").append(toIndentedString(orgID)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  public class TelegrafRequestPluginsAdapter implements JsonDeserializer<Object>, JsonSerializer<Object> {

    public TelegrafRequestPluginsAdapter() {
    }

    @Override
    public Object deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {

      List<String> discriminator = Arrays.asList("name", "type");

      List<Object> results = new ArrayList<>();

      for (JsonElement arrayItem: json.getAsJsonArray()){
        JsonObject jsonObject = arrayItem.getAsJsonObject();

        String[] types = discriminator.stream().map(d -> jsonObject.get(d).getAsString()).toArray(String[]::new);

        results.add(deserialize(types, jsonObject, context));
      }

      return results;
    }

    @Override
    public JsonElement serialize(Object object, Type typeOfSrc, JsonSerializationContext context) {

      return context.serialize(object);
    }

    private Object deserialize(final String[] types, final JsonElement json, final JsonDeserializationContext context) {

      if (Arrays.equals(new String[]{ "cpu", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputCpu.class);
      }
      if (Arrays.equals(new String[]{ "disk", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputDisk.class);
      }
      if (Arrays.equals(new String[]{ "diskio", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputDiskio.class);
      }
      if (Arrays.equals(new String[]{ "docker", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputDocker.class);
      }
      if (Arrays.equals(new String[]{ "file", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputFile.class);
      }
      if (Arrays.equals(new String[]{ "kubernetes", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputKubernetes.class);
      }
      if (Arrays.equals(new String[]{ "logparser", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputLogParser.class);
      }
      if (Arrays.equals(new String[]{ "procstat", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputProcstat.class);
      }
      if (Arrays.equals(new String[]{ "prometheus", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputPrometheus.class);
      }
      if (Arrays.equals(new String[]{ "redis", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputRedis.class);
      }
      if (Arrays.equals(new String[]{ "syslog", "input" }, types)) {
        return context.deserialize(json, TelegrafPluginInputSyslog.class);
      }
      if (Arrays.equals(new String[]{ "file", "output" }, types)) {
        return context.deserialize(json, TelegrafPluginOutputFile.class);
      }
      if (Arrays.equals(new String[]{ "influxdb_v2", "output" }, types)) {
        return context.deserialize(json, TelegrafPluginOutputInfluxDBV2.class);
      }

      return context.deserialize(json, Object.class);
    }
  }
}

