package sample;

public class Person {

    private String id,name,code,imageReference;

    public Person(String id, String name, String code, String imageReference) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.imageReference = imageReference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImageReference() {
        return imageReference;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }
}
