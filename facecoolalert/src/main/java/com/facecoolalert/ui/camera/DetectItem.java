package com.facecoolalert.ui.camera;

import com.facecoolalert.database.entities.Subject;

public class DetectItem {
    public Subject subject;
    public String path;
    public String ref;

    public Boolean isSpecial=false;

    public String getName() {
        return subject == null ? "" : subject.getName();
    }
}
