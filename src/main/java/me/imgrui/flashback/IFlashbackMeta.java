package me.imgrui.flashback;

import java.io.File;

public interface IFlashbackMeta {
    void setLodPath(File path);
    File getLodPath();
    void setSavedLods(boolean savedLods);
    boolean getSavedLods();
}