package com.fise.marechat.function.photo;

import java.io.Serializable;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/18
 * @time 10:49
 */
public class PhotoModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalPath;
    private boolean isChecked;

    public PhotoModel(String originalPath, boolean isChecked) {
        super();
        this.originalPath = originalPath;
        this.isChecked = isChecked;
    }

    public PhotoModel(String originalPath) {
        this.originalPath = originalPath;
    }

    public PhotoModel() {
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        System.out.println("checked " + isChecked + " for " + originalPath);
        this.isChecked = isChecked;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((originalPath == null) ? 0 : originalPath.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PhotoModel)) {
            return false;
        }
        PhotoModel other = (PhotoModel) obj;
        if (originalPath == null) {
            if (other.originalPath != null) {
                return false;
            }
        } else if (!originalPath.equals(other.originalPath)) {
            return false;
        }
        return true;
    }

}