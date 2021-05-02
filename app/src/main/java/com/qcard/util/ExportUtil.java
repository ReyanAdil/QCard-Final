package com.qcard.util;

import com.qcard.data.model.QCard;
import com.qcard.data.model.QSet;
import com.qcard.data.model.SharableQCard;
import com.qcard.data.model.SharableQSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExportUtil {
    public static SharableQSet createSharableQSet(QSet set, List<QCard> cards) {
        SharableQSet sharableQSet = new SharableQSet();
        sharableQSet.setName(set.getName());
        sharableQSet.setDescription(set.getDescription());

        ArrayList<SharableQCard> sharableQCards = new ArrayList<>();
        for (QCard card : cards) {
            SharableQCard sharableQCard = new SharableQCard();
            sharableQCard.setTerm(card.getTerm());
            sharableQCard.setDefinition(card.getDefinition());
            sharableQCard.setDefinitionImage(card.getDefinitionImage());
            sharableQCard.setTermImage(card.getTermImage());

            sharableQCards.add(sharableQCard);
        }

        sharableQSet.setCards(sharableQCards);
        return sharableQSet;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}
