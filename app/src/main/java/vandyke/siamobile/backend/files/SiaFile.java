/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SiaFile {

    private String siapath;
    private long filesize; // is long enough?
    private boolean available;
    private boolean renewing;
    private int redundancy;
    private int uploadProgress;
    private int expiration;

    public SiaFile(JSONObject json) {
        try {
            siapath = json.getString("siapath");
            filesize = json.getLong("filesize");
            available = json.getBoolean("available");
            renewing = json.getBoolean("renewing");
            redundancy = json.getInt("redundancy");
            uploadProgress = json.getInt("uploadprogress");
            expiration = json.getInt("expiration");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<SiaFile> getSiaFiles(JSONObject json) {
        ArrayList<SiaFile> result = new ArrayList<>();
        try {
            JSONArray filesArray = json.getJSONArray("files");
            for (int i = 0; i < filesArray.length(); i++)
                result.add(new SiaFile(filesArray.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getSiapath() {
        return siapath;
    }

    public long getFilesize() {
        return filesize;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isRenewing() {
        return renewing;
    }

    public int getRedundancy() {
        return redundancy;
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public int getExpiration() {
        return expiration;
    }
}
