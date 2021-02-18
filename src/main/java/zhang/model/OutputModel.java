package zhang.model;

import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.template.annotations.FieldMetadata;
import fr.opensagres.xdocreport.template.annotations.ImageMetadata;


public class OutputModel {
    private String message;
    private IImageProvider image;
    private String filename;

    public OutputModel(String message, IImageProvider image, String filename) {
        this.message = message;
        this.image = image;
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    @FieldMetadata(images = {@ImageMetadata( name = "image")}, description = "image")
    public IImageProvider getImage() {
        return image;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImage(IImageProvider image) {
        this.image = image;
    }
}
