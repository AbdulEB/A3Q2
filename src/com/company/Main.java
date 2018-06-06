package com.company;

/* Author: Abdul El Badaoui
 * Student Number: 5745716
 * Description: This programs is the vigenere cipher. it finds the key length m, and uses the chi-squared statistics
 * method to decipher the message.
 * */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    //parameters
    static String [] fileInput;
    static String cipherText, plainText;
    static int m;
    static Scanner in;
    static Map<String, Double> expectedLetterFrequency;

    public static void main(String[] args) {
        //initialize the parameters
        initialize();
        m = getKeyLength();//get the m, which is the key length

        //breaks the cipher text to the respected sequences - m
        String [] mSequences = new String[m];
        for (int i = 0; i<m; i++){
            String mSequence = "";
            for (int j = i; j<cipherText.length(); j+=m){
                mSequence+=cipherText.charAt(j);
            }
            mSequences[i] = mSequence;
        }

        String key = getKey(mSequences);//get key
        decipherMessage(key);//decrypt the message
        printOut();


    }

    //method that initialize the parameters
    public static void initialize(){
        try {
            fileInput = Files.readAllLines(Paths.get("a3q2in.txt")).toArray(new String[0]);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        plainText = "";
        cipherText = "";
        for (int i = 0; i<fileInput.length; i++){
            cipherText+=fileInput[i];
        }

        expectedLetterFrequency = new HashMap<>();
        try {
            in = new Scanner(new FileInputStream(new File("expectedLetterFrequencies.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(in.hasNext()){
            expectedLetterFrequency.put(in.next(), in.nextDouble());
        }
    }

    //method gets the key length
    public static int getKeyLength(){
        //initialize the local method parameters
        double indexOfCoincidence = 0.0, sum;
        double approxValue = Double.MAX_VALUE;
        int result = 0;
        //loop to check which the key length between legth of 1 to 5
        for (int i = 1; i<=5; i++){
            sum = 0.0;
            double [] mValues = new double[i];//get the m value for each length from 1 to 5
            for (int j = 0; j<i; j++ ){
                String mSequence =  "";//the initialized split sequence
                for (int k = j; k<cipherText.length(); k+=i){
                    mSequence+=cipherText.charAt(k);
                }
                mValues[j] = getIndexOfCoincidence(mSequence);//get the index of coincidence
            }
            for (int j = 0; j<mValues.length; j++){
                sum+= mValues[j];
            }
            double temp = sum/i;// gets the average of all the m values per each length
            if (Math.abs(temp - 0.065) < approxValue){//the closet length average to 0.065 is the Index of Coincidence
                result = i;
                approxValue = Math.abs(temp- 0.065);
                indexOfCoincidence = temp;
            }
        }
        return result;

    }
    //method to get the index of coincidence (uses the the formula that was taught in class)
    public static double getIndexOfCoincidence(String mSequence){

        double value = 0.0;
        int n = mSequence.length();

        Map<String, Integer> characterFrequency = letterFrequency(mSequence);

        double IcDenominator = (n*(n-1));
        for (String key: characterFrequency.keySet()){
            int f = characterFrequency.get(key);
            double IcNumerator = (f*(f-1));
            value += (double) IcNumerator/IcDenominator;
        }

        return value;
    }

    //method that solves the key
    public static String getKey(String [] mSequencies){
        //declare the local parameters
        String result = "";
        double chiSquaredStatistic;
        int shiftCharacter;
        int run =0;
        int shiftedCharValue;

        //loop that uses the chi-squared statistic to get the characters for the key
        while (run<m){
            chiSquaredStatistic = Double.MAX_VALUE;
            shiftCharacter = -1;

            for(int i = 0 ; i<26; i++){
                String shiftedCharSet = "";
                //get the shifted character set
                for (int j = 0; j<mSequencies[run].length(); j++){
                    shiftedCharValue = ((mSequencies[run].charAt(j) - 65) +i)%26 + 65;
                    shiftedCharSet+= (char) shiftedCharValue;
                }

                Map<String, Integer> charFrequency = letterFrequency(shiftedCharSet);//finds the character set frequency
                double chiSquare = 0.0;

                //ch-square algorithm
                for (int j = 0; j<26; j++){
                    char character = (char) (j+65);
                    if(charFrequency.containsKey(""+character)){
                        int sequenceCount = charFrequency.get(""+character);
                        double expectedCount = expectedLetterFrequency.get(""+character)*shiftedCharSet.length();
                        chiSquare += Math.pow((sequenceCount-expectedCount), 2.0)/expectedCount;
                    }

                }
                //finds the lowest value of the chi-square and the shift character
                if (chiSquare<chiSquaredStatistic){
                    chiSquaredStatistic = chiSquare;
                    shiftCharacter = i;
                }

            }
            result += (char)(shiftCharacter+65);//add the char to the string that will be returned
            run++;

        }


        return  result;
    }

    //method that will get the letter frequency per shift subSequence
    public static Map<String, Integer> letterFrequency( String text){
        char [] charFrequencyArray = text.toCharArray();
        int [] charCount = new int[127];
        Map<String, Integer> letterFrequency = new HashMap<>();

        for (int i = 0; i < charFrequencyArray.length; i++){
            charCount[charFrequencyArray[i]]++;
        }

        for (int i = 65; i < 91; i++){
            if (charCount[i] != 0) letterFrequency.put(""+((char) i), charCount[i]);
        }

        return letterFrequency;
    }

    //method that prints out the decrypted message in a text file
    public static void printOut(){
        try {
            PrintWriter fileOutput = new PrintWriter("a3q2out.txt", "UTF-8");// output file creation
            fileOutput.println("The secret message (plaintext) is: ");
            fileOutput.println();
            fileOutput.println(plainText);
            fileOutput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //decipher method for vigenere -->shift cipher
    public static void decipherMessage(String key){
        int cipherCharacterValue, keyCharValue;
        for (int i = 0; i<cipherText.length(); i++){
            cipherCharacterValue = cipherText.charAt(i) -65;
            keyCharValue = key.charAt(i%key.length()) - 65;
            plainText+= (char)((cipherCharacterValue+keyCharValue)%26 +65);
        }
    }

}
