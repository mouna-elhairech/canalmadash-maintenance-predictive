package com.canal.canalmadash.dto;

public class AiResponseSection {

    /*
     * Petit bloc de reponse IA.
     * Le frontend peut afficher chaque section avec un titre et un contenu
     * au lieu d'avoir un seul grand texte difficile a lire.
     */
    private String title;
    private String content;

    public AiResponseSection() {
    }

    public AiResponseSection(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
