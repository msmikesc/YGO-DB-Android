package ygodb.windows.importer;

import nu.pattern.OpenCV;
import ygodb.commonlibrary.bean.GamePlayCard;
import ygodb.commonlibrary.connection.SQLiteConnection;
import ygodb.commonlibrary.utility.YGOLogger;
import ygodb.windows.utility.WindowsUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class DownloadCardImagesByMissing {
	boolean shouldUseComputerVision = false;

	public static void main(String[] args) throws SQLException, InterruptedException {

		DownloadCardImagesByMissing mainObj = new DownloadCardImagesByMissing();
		SQLiteConnection db = WindowsUtil.getDBInstance();

		YGOLogger.info("Import Started");
		boolean successful = mainObj.run(db);
		if (!successful) {
			YGOLogger.info("Import Failed");
		} else {
			YGOLogger.info("Import Finished");
		}
		db.closeInstance();
	}

	public boolean run(SQLiteConnection db) throws SQLException, InterruptedException {
		//get alt art ids UNION passcode
		Set<Integer> dbPasscodes = new HashSet<>(db.getAllArtPasscodes());
		Set<Integer> fileSystemPicCodes = getIntegersFromFilenames(
				"C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\app\\src\\main\\assets\\pics");

		dbPasscodes.removeAll(fileSystemPicCodes);

		if(shouldUseComputerVision) {
			OpenCV.loadLocally();
			Set<Integer> fileSystemPicCodesBackImage = getSimilarImages(
					"C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\app\\src\\main\\assets\\pics",
					"C:\\Users\\Mike\\AndroidStudioProjects\\YGODB\\app\\src\\main\\assets\\pics\\-1.jpg");
			dbPasscodes.addAll(fileSystemPicCodesBackImage);
		}

		List<GamePlayCard> artCardsList = new ArrayList<>();

		for (Integer passcode : dbPasscodes) {
			GamePlayCard newCard = new GamePlayCard();
			newCard.setCardName(String.valueOf(passcode));
			newCard.setPasscode(passcode);
			artCardsList.add(newCard);
		}

		return WindowsUtil.downloadAllCardImagesForList(artCardsList);
	}

	public static Set<Integer> getIntegersFromFilenames(String directoryPath) {
		HashSet<Integer> integers = new HashSet<>();
		File directory = new File(directoryPath);

		if (!directory.isDirectory()) {
			YGOLogger.info("The provided path is not a directory.");
			return integers;
		}

		// Regex pattern to match filenames like "<positiveinteger>.jpg"
		Pattern pattern = Pattern.compile("^(\\d+)\\.jpg$");

		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				Matcher matcher = pattern.matcher(file.getName());
				if (matcher.matches()) {
					integers.add(Integer.parseInt(matcher.group(1)));
				}
			}
		}

		return integers;
	}

	public static Set<Integer> getSimilarImages(String directoryPath, String inputImagePath) {
		File dir = new File(directoryPath);
		File[] files = dir.listFiles((dir1, name) -> name.matches("\\d+\\.jpg"));
		HashSet<Integer> similarImages = new HashSet<>();

		if (files == null) {
			return similarImages;
		}

		Mat inputImage = Imgcodecs.imread(inputImagePath);
		Mat inputImageGray = new Mat();
		Imgproc.cvtColor(inputImage, inputImageGray, Imgproc.COLOR_BGR2GRAY);

		ORB orb = ORB.create();
		MatOfKeyPoint keyPointsInput = new MatOfKeyPoint();
		Mat descriptorsInput = new Mat();
		orb.detectAndCompute(inputImageGray, new Mat(), keyPointsInput, descriptorsInput);

		for (File file : files) {
			String fileName = FilenameUtils.getBaseName(file.getName());
			int fileNumber = Integer.parseInt(fileName);

			Mat currentImage = Imgcodecs.imread(file.getAbsolutePath());
			Mat currentImageGray = new Mat();
			Imgproc.cvtColor(currentImage, currentImageGray, Imgproc.COLOR_BGR2GRAY);

			MatOfKeyPoint keyPointsCurrent = new MatOfKeyPoint();
			Mat descriptorsCurrent = new Mat();
			orb.detectAndCompute(currentImageGray, new Mat(), keyPointsCurrent, descriptorsCurrent);

			DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
			MatOfDMatch matches = new MatOfDMatch();
			matcher.match(descriptorsInput, descriptorsCurrent, matches);

			DMatch[] matchArray = matches.toArray();
			int goodMatchesCount = 0;
			for (DMatch match : matchArray) {
				if (match.distance < 50) {
					goodMatchesCount++;
				}
			}

			if (goodMatchesCount > 20) { // Threshold for similarity, can be adjusted
				similarImages.add(fileNumber);
			}
		}

		return similarImages;
	}
}
