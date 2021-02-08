package com.amazonaws.samples;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.Emotion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


public class FacialSentimentAnalysis extends JFrame {
	
	//Global variables
	private static final long serialVersionUID = 1L;
	private JPanel emotionPanel;
	public static String emotionGuess = null;
	public static boolean guessMade = false;
	
	//Create instances of Rekognition API and S3 for use in the application		
	static AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	public static AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
	public static String s3Bucket = "empathyprototype-deployments-mobilehub-595834428";

	
	
	//Constructor for image/choice windows
	public FacialSentimentAnalysis(S3ObjectSummary summary) throws IOException {
		
		//Create question panel
		JLabel question = new JLabel("Which emotion is this person exhibiting?", SwingConstants.CENTER);
		question.setFont(question.getFont().deriveFont(20.0f));
		
		//Create and style image panel
		JPanel imagePanel = new JPanel();
		URL imageUrl = new URL("https://s3.amazonaws.com/empathyprototype-deployments-mobilehub-595834428/" + summary.getKey());
	    JLabel label = new JLabel(new ImageIcon((ImageIO.read(imageUrl))));
	    label.setPreferredSize(new Dimension(500, 380));
	    Border border = BorderFactory.createBevelBorder(1, Color.black, Color.black);
		label.setBorder(border);
	    imagePanel.add(label);
	    
	    //Create panel with emotion choices
	    buildEmotionPanel();	
	    
	    //Add panels to JFrame
	    add(question, BorderLayout.NORTH);
	    add(imagePanel, BorderLayout.CENTER);
	    add(emotionPanel, BorderLayout.SOUTH);
	    
	    //Create and style JFrame
	    setTitle("Empathy");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
	    pack();
	    setVisible(true);
	    Dimension windowDimension = Toolkit.getDefaultToolkit().getScreenSize();
	    setLocation(windowDimension.width/2-this.getSize().width/2, windowDimension.height/2-this.getSize().height/2);
	}	
	
	
	
	//Assemble panel for emotion choice buttons
	private void buildEmotionPanel() {
		
		emotionPanel = new JPanel();
		
		JButton happy = new JButton("Happy");
		happy.setActionCommand("HAPPY");
		happy.addActionListener(new EmotionListener());
	    emotionPanel.add(happy);
	    
	    JButton sad = new JButton("Sad");
	    sad.setActionCommand("SAD");
	    sad.addActionListener(new EmotionListener());
	    emotionPanel.add(sad);
	    
	    JButton calm = new JButton("Calm");
	    calm.setActionCommand("CALM");
	    calm.addActionListener(new EmotionListener());
	    emotionPanel.add(calm);
	    
	    JButton disgusted = new JButton("Disgusted");
	    disgusted.setActionCommand("DISGUSTED");
	    disgusted.addActionListener(new EmotionListener());
	    emotionPanel.add(disgusted);
	    
	    JButton scared = new JButton("Scared");
	    scared.setActionCommand("SCARED");
	    scared.addActionListener(new EmotionListener());
	    emotionPanel.add(scared);
	    
	    JButton confused = new JButton("Confused");
	    confused.setActionCommand("CONFUSED");
	    confused.addActionListener(new EmotionListener());
	    emotionPanel.add(confused);
	    
	    JButton angry = new JButton("Angry");
	    angry.setActionCommand("ANGRY");
	    angry.addActionListener(new EmotionListener());
	    emotionPanel.add(angry);
	}	
	
	
	
	//When an emotion is selected, this listener assigns the selection to emotionGuess and indicates that a selection was made 
	private class EmotionListener implements ActionListener {		
		public void actionPerformed(ActionEvent e) {	
			emotionGuess = e.getActionCommand();
			guessMade = true;
		}
	}
	
	
	
	//Analyzes an image using the AWS Rekognition API
	public static void analyzeImage(S3ObjectSummary summary) {
		
		//Create a request for the Rekognition API 
		Image image = new Image().withS3Object(new S3Object().withName(summary.getKey()).withBucket(s3Bucket));
		DetectFacesRequest request = new DetectFacesRequest().withImage(image).withAttributes(Attribute.ALL);
		
		try {
					
			//Call the Rekognition API and store resulting emotions of the analysis in a List
			DetectFacesResult result = rekognitionClient.detectFaces(request); 
			List <Emotion> emotions = result.getFaceDetails().get(0).getEmotions();
				
			float highestConfidenceLevel = 0.0F;							 
			String mostLikelyEmotion = null;			
			
			//Determine the most likely emotion based on the highest confidence level
			for (Emotion emotion : emotions) {
				if (emotion.getConfidence() > highestConfidenceLevel) {
					mostLikelyEmotion = emotion.getType();
					highestConfidenceLevel = emotion.getConfidence();
				}
			}	
			
			//Loop until user makes a guess, then compare this guess with Rekognition's result
			while (true) {
				System.out.println();
				if (guessMade) {
					
					//Display if the guess was correct or not
					if (emotionGuess.equals(mostLikelyEmotion)) 
						JOptionPane.showMessageDialog(null, "Correct! You guessed: " 
								+ mostLikelyEmotion.charAt(0) + mostLikelyEmotion.substring(1).toLowerCase());
					else
						JOptionPane.showMessageDialog(null, "Oops! That's not right. The correct emotion was: " 
								+ mostLikelyEmotion.charAt(0) + mostLikelyEmotion.substring(1).toLowerCase());
					break;
				}
			}		
			
			//set global variables back to default
			emotionGuess = null;
			guessMade = false;				
		} 	
		
		catch (AmazonRekognitionException err) {
			err.printStackTrace();
		}
	}	
	
	
	
	//Main method. Builds windows and calls the analyze function, then disposes of window once result is displayed
	public static void main(String[] args) throws Exception {
		
		//Iterate through each image in the S3 Bucket
		for (S3ObjectSummary summary : S3Objects.withPrefix(s3Client, s3Bucket, null)) {
			FacialSentimentAnalysis windowInstance = new FacialSentimentAnalysis(summary);
			analyzeImage(summary);
			windowInstance.dispose();
		}
		
		System.exit(0);
	}
}  