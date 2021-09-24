package sample;

public class Person {

    private String id,name,code,imageReference,lista,pod;

    public Person(String id, String name, String code, String imageReference, String lista, String pod) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.imageReference = imageReference;
        this.lista = lista;
        this.pod = pod;
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

    public String getLista() {
        return lista;
    }

    public void setLista(String lista) {
        this.lista = lista;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public void setImageReference(String imageReference) {
        this.imageReference = imageReference;
    }
}
