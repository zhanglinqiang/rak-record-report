package zhang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgInfo {
    private String filename;
    private String filepath;
    private Date date;
    private String personName;
    private String dirName;
}
