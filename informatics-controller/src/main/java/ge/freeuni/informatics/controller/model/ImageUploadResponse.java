package ge.freeuni.informatics.controller.model;

public class ImageUploadResponse extends InformaticsResponse {

    private String imageUrl;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageUploadResponse(boolean failed, String message) {
        super(message);
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
