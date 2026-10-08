package ge.freeuni.informatics.controller.model;

public class EditorialResponse extends InformaticsResponse {
    String editorial;

    public EditorialResponse(String editorial) {
        super(null);
        this.editorial = editorial;
    }

    public String getEditorial() {
        return editorial;
    }
}
