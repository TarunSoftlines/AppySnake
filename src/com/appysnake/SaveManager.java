package com.appysnake;

import javax.microedition.rms.RecordStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class SaveManager {

    private static final String RS_NAME = "AppySnakeSaveData";

    public void saveProgress(int level, int score) {
        RecordStore rs = null;
        try {
            // Delete old record store to write fresh data
            try {
                RecordStore.deleteRecordStore(RS_NAME);
            } catch (Exception e) {
                // Ignore if record store didn't exist yet
            }

            rs = RecordStore.openRecordStore(RS_NAME, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeInt(level);
            dos.writeInt(score);
            dos.flush();

            byte[] data = baos.toByteArray();
            rs.addRecord(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    public int[] loadProgress() {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(RS_NAME, false);
            if (rs != null && rs.getNumRecords() > 0) {
                byte[] data = rs.getRecord(1);
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                DataInputStream dis = new DataInputStream(bais);

                int level = dis.readInt();
                int score = dis.readInt();

                return new int[]{level, score};
            }
        } catch (Exception e) {
            // No saved game found or unreadable
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        return null;
    }
}
