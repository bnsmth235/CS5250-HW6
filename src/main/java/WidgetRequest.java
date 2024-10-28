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

    // Getters and Setters

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public String getWidgetId() {
        return widgetId;
    }

    public String getOwner() {
        return owner;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
