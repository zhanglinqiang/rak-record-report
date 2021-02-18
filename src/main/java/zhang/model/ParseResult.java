package zhang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParseResult {
    private List<ImgInfo> imgInfos;
    private List<String> errorMessage;
}
