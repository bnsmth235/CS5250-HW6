import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class WidgetRequest {
    @JsonProperty("type")
    private String type;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("widgetId")
    private String widgetId;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("label")
    private String label;
    @JsonProperty("description")
    private String description;
    @JsonProperty("otherAttributes")
    private List<OtherAttribute> otherAttributes;

    public void replaceNullFields() {
        if (this.type == null) this.type = "";
        if (this.requestId == null) this.requestId = "";
        if (this.widgetId == null) this.widgetId = "";
        if (this.owner == null) this.owner = "";
        if (this.label == null) this.label = "";
        if (this.description == null) this.description = "";
        if (this.otherAttributes != null) {
            for (OtherAttribute attr : this.otherAttributes) {
                if (attr.getName() == null) attr.setName("");
                if (attr.getValue() == null) attr.setValue("");
            }
        }
        if (this.otherAttributes == null){
            this.otherAttributes = List.of();
        }
    }

    public static WidgetRequest fromJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if(json == null || json.isEmpty()) {
                return new WidgetRequest();
            }
            WidgetRequest request = mapper.readValue(json, WidgetRequest.class);
            request.replaceNullFields();
            return request;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson() {
        replaceNullFields();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OtherAttribute> getOtherAttributes() {
        return otherAttributes;
    }

    public void setOtherAttributes(List<OtherAttribute> otherAttributes) {
        this.otherAttributes = otherAttributes;
    }

    public static class OtherAttribute {
        @JsonProperty("name")
        private String name;
        @JsonProperty("value")
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}