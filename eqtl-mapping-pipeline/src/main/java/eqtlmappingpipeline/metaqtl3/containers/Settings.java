/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eqtlmappingpipeline.metaqtl3.containers;

import eqtlmappingpipeline.Main;
import eqtlmappingpipeline.metaqtl3.FDR.FDRMethod;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import umcg.genetica.io.Gpio;
import umcg.genetica.io.text.TextFile;
import umcg.genetica.io.trityper.TriTyperGeneticalGenomicsDatasetSettings;
import umcg.genetica.io.trityper.util.ChrAnnotation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author harmjan
 */
public class Settings extends TriTyperGeneticalGenomicsDatasetSettings {

	// Output
	public String settingsTextToReplace = null;                                // Replace this text in the configuration XML file
	public String settingsTextReplaceWith = null;                              // Replace the text in settingsTextToReplace with this text
	public boolean createSNPPValueSummaryStatisticsFile = false;               // Output SNP P-Value summary statistics
	public boolean createSNPSummaryStatisticsFile = false;                     // Output SNP P-Value summary statistics
	public boolean createEQTLPValueTable = false;                              // Output an eQTL p-value table (only applies for a limited number (500) of SNP)
	public String outputReportsDir;                                            // Output directory for reports
	// SNP QC
	// Analysis settings
	public boolean performParametricAnalysis = false;                          // Perform parametric analysis
	public boolean useAbsoluteZScorePValue = false;                            // Use absolute Z-score? (required for finding opposite allelic effects)
	public int ciseQTLAnalysMaxSNPProbeMidPointDistance = 250000;              // Midpoint distance for declaring an eQTL effect CIS
	public int maxNrMostSignificantEQTLs = 500000;                             // Max number of results stored in memory
	public boolean performParametricAnalysisGetAccuratePValueEstimates;        // Use an accurate estimation of the P-values
	public Integer nrThreads;                                                  // Use this number of threads
	public boolean writeSNPQCLog = false;
	// Multiple testing correction
	public double fdrCutOff = 0.05;                                            // Cutoff for FDR procedure
	public int nrPermutationsFDR = 1;                                          // Number of permutations to determine FDR
	public FDRMethod fdrType = FDRMethod.ALL;                                 // Type of FDRs to calculate
	public boolean fullFdrOutput = true;                                      // Skip out on large FDR files
	// confinements
	public boolean performEQTLAnalysisOnSNPProbeCombinationSubset;             // Confine to a certain set of probe/snp combinations?
	public Byte confineToSNPsThatMapToChromosome;                              // Confine SNP to be assessed to SNPs mapped on this chromosome
	public boolean expressionDataLoadOnlyProbesThatMapToChromosome = false;    // Only load expression data for probes with a known chromosome mapping
	public HashSet<String> tsSNPsConfine = null;                               // Confine analysis to the SNPs in this hash
	public HashMap<String, HashSet<String>> tsSNPProbeCombinationsConfine;     // Confine analysis to the combinations of SNP and Probes in this hash
	// plots
	public double plotOutputPValueCutOff = -1;                                      // Use this p-value as a cutoff for drawing plots [defaults to off]
	public String plotOutputDirectory;                                         // Print the plots in this directory
	public boolean runOnlyPermutations = false;
	public Integer startWithPermutation;
	public Boolean confineSNPsToSNPsPresentInAllDatasets = true;
	public int requireAtLeastNumberOfDatasets = 1;
	public boolean confineProbesToProbesPresentInAllDatasets;
	public ArrayList<TriTyperGeneticalGenomicsDatasetSettings> datasetSettings;
	public String regressOutEQTLEffectFileName;
	public boolean regressOutEQTLEffectsSaveOutput;
	public double snpQCCallRateThreshold = 0.95;
	public double snpQCHWEThreshold = 0.0001;
	public double snpQCMAFThreshold = 0.05;
	public Byte confineToProbesThatMapToChromosome;
	public boolean createBinaryOutputFiles;
	public boolean createTEXTOutputFiles;
	public String strConfineSNP;
	public String strConfineProbe;
	public String strConfineSNPProbe;
	public boolean provideFoldChangeData = false;
	public boolean provideBetasAndStandardErrors = true;
	public boolean equalRankForTies = true;
	public boolean createQQPlot = true;
	public boolean createDotPlot = true;
	public boolean metaAnalyseInteractionTerms = false;
	public boolean metaAnalyseModelCorrelationYHat = false;
	public String pathwayDefinition = null;
	public boolean snpProbeConfineBasedOnChrPos = false; //Snp in snp confine and snp probe confine list are defined as chr:pos instead of snp ID.
	private static final Pattern TAB_PATTERN = Pattern.compile("\\t");
	public boolean permuteCovariates;
	public long rSeed = System.currentTimeMillis();
	public Random randomNumberGenerator = new Random(rSeed);
	public Integer batchid;
	public Integer batchsize;
	public boolean displayWarnings = true;
	public int numberOfVariantsToBuffer = 1000;
	public boolean skipFDRCalculation = false;
	public boolean usemd5hash = true;
	public boolean sortsnps = false;
	public boolean dumpeverythingtodisk;
	public Integer stopWithPermutation;
	public boolean createBinaryFilesOnlyMetaAnalysis;
	public boolean omitDatasetSummaryStats = false;

	public Settings() {
	}

	public void load(String settings) throws IOException, ConfigurationException {

		XMLConfiguration config = null;
		if (settingsTextToReplace != null) {

//			System.out.println("Found string to replace: " + settingsTextToReplace);
//			System.out.println("Replacement string: " + settingsTextReplaceWith);

			String[] queries = settingsTextToReplace.split(",");
			String[] replacements = settingsTextReplaceWith.split(",");

			if (queries.length != replacements.length) {
				System.out.println("Issue with replacement strings!");
				System.out.println("Number of strings to replace not equal!");
				System.exit(-1);
			}

			for (int q = 0; q < queries.length; q++) {
				System.out.println("Will replace " + queries[q] + " with " + replacements[q]);
			}

			System.out.println("Will attempt to replace template strings in configuration file.");
			System.out.println(queries.length + " strings to replace with " + replacements.length + " replacements");
			if (replacements.length != queries.length) {
				System.out.println("Error: number of strings to replace and number of replacements should be equal.");
				System.exit(-1);
			}

			TextFile tf = new TextFile(settings, TextFile.R);
			String generatedString = RandomStringUtils.randomAlphabetic(12);
//			String generatedString = "v2";//RandomStringUtils.randomAlphabetic(12);
			TextFile tf2 = new TextFile(settings + "-" + generatedString + ".xml", TextFile.W);
			String line = tf.readLine();
			int lnctr = 0;
			while (line != null) {
				for (int s = 0; s < queries.length; s++) {
					String query = queries[s];
					String replacement = replacements[s];
//					System.out.println(lnctr + "\t" + query + "\t" + replacement);
					if (line.contains(query)) {
//						System.out.println(line + " --> Replacing: " + query + " with " + replacement);
						line = line.replace(query, replacement);
					}

				}
				tf2.writeln(line);
				lnctr++;
				line = tf.readLine();
			}
			tf.close();
			tf2.close();

			config = new XMLConfiguration(settings + "-" + generatedString + ".xml");           // Use the apache XML configuration parser
			Files.delete(Paths.get(settings + "-" + generatedString + ".xml"));
		} else {
			config = new XMLConfiguration(settings);           // Use the apache XML configuration parser
		}


		String analysisType = null;
		String correlationType = null;
		Boolean useAbsPVal = null;
		Integer nrthread = null;
		Integer cisDist = null;
		Double MAF = null;
		Double HWE = null;
		Double callrate = null;
		String correctiontype = null;
		Integer randomseed = null;
		String fdrtype = "probe";
		boolean largeFdrFileOut = true;
		Double mtThreshold = null;
		Integer numPermutations = null;
		String outdir = null;
		Double outputplotthreshold = null;
		String outputplotdirectory = null;

		// load the defaults:
		// QC defaults
		try {
			callrate = config.getDouble("defaults.qc.snpqccallratethreshold");
		} catch (Exception e) {
		}
		if (callrate != null) {
			snpQCCallRateThreshold = callrate;
		} else {
			snpQCCallRateThreshold = 0.95;
		}

		try {
			HWE = config.getDouble("defaults.qc.snpqchwethreshold");
		} catch (Exception e) {
		}

		if (HWE != null) {
			snpQCHWEThreshold = HWE;
		} else {
			snpQCHWEThreshold = 0.0000001;
		}

		try {
			MAF = config.getDouble("defaults.qc.snpqcmafthreshold");
		} catch (Exception e) {
		}
		if (MAF != null) {
			snpQCMAFThreshold = MAF;
		} else {
			snpQCMAFThreshold = 0.05;
		}


		try {
			sortsnps = config.getBoolean("defaults.analysis.sortsnps", false);
		} catch (Exception e) {

		}
		// analysis settings
		try {
			String buffersizeStr = config.getString("defaults.analysis.buffersize");
			this.numberOfVariantsToBuffer = Integer.parseInt(buffersizeStr);
		} catch (Exception e) {
		}


		try {
			createQQPlot = config.getBoolean("defaults.analysis.createqqplot", true);
		} catch (Exception e) {
		}

		try {
			createDotPlot = config.getBoolean("defaults.analysis.createdotplot", true);
		} catch (Exception e) {
		}


		try {
			runOnlyPermutations = config.getBoolean("defaults.analysis.onlypermutations", false);
		} catch (Exception e) {
		}

		// analysis settings
		try {
			analysisType = config.getString("defaults.analysis.analysistype");
		} catch (Exception e) {
		}

		if (analysisType != null) {
			if (analysisType.toLowerCase().equals("cis")) {
				cisAnalysis = true;
				transAnalysis = false;
			} else if (analysisType.toLowerCase().equals("trans")) {
				cisAnalysis = false;
				transAnalysis = true;
			} else if (analysisType.toLowerCase().equals("cistrans")) {
				cisAnalysis = true;
				transAnalysis = true;
			}
		} else {
			cisAnalysis = true;
			transAnalysis = false;
		}

		try {
			cisDist = config.getInteger("defaults.analysis.cisanalysisprobedistance", null);
		} catch (Exception e) {
		}
		if (cisDist != null) {
			ciseQTLAnalysMaxSNPProbeMidPointDistance = cisDist;
		} else {
			ciseQTLAnalysMaxSNPProbeMidPointDistance = 250000;
		}

		try {
			correlationType = config.getString("defaults.analysis.correlationtype", null);
		} catch (Exception e) {
		}
		if (correlationType != null) {
			if (correlationType.toLowerCase().equals("parametric")) {
				performParametricAnalysis = true;
			} else {
				performParametricAnalysis = false;
			}
		} else {
			performParametricAnalysis = true;
		}

		Boolean useIdenticalRanksForTies = null;
		try {
			useIdenticalRanksForTies = config.getBoolean("defaults.analysis.equalrankforties", null);
		} catch (Exception e) {
		}
		if (useIdenticalRanksForTies != null) {
			equalRankForTies = useIdenticalRanksForTies;
		} else {
			equalRankForTies = true;
		}

        /*
		 public boolean metaAnalyseInteractionTerms = false;
         public boolean metaAnalyseModelCorrelationYHat = false;
         */
		metaAnalyseInteractionTerms = false;
		Boolean metaAnalyzeInteractionTermsB = null;
		try {
			metaAnalyzeInteractionTermsB = config.getBoolean("defaults.analysis.metaAnalyseInteractionTerms", null);
			metaAnalyseInteractionTerms = metaAnalyzeInteractionTermsB;
		} catch (Exception e) {
			metaAnalyseInteractionTerms = false;
		}

		permuteCovariates = false;
		try {
			permuteCovariates = config.getBoolean("defaults.analysis.permuteCovariates", false);
		} catch (Exception e) {
		}
		if (metaAnalyzeInteractionTermsB != null) {
			metaAnalyseInteractionTerms = metaAnalyzeInteractionTermsB;
		} else {
			metaAnalyseInteractionTerms = false;
		}


		Boolean metaAnalyseModelCorrelationYHatB = null;
		try {
			metaAnalyseModelCorrelationYHatB = config.getBoolean("defaults.analysis.metaAnalyseModelCorrelationYHat", null);
		} catch (Exception e) {
		}
		if (metaAnalyseModelCorrelationYHatB != null) {
			metaAnalyseModelCorrelationYHat = metaAnalyseModelCorrelationYHatB;
		} else {
			metaAnalyseModelCorrelationYHat = false;
		}

		try {
			useAbsPVal = config.getBoolean("defaults.analysis.useabsolutepvalue", false);
		} catch (Exception e) {
		}

		if (useAbsPVal == null) {
			useAbsoluteZScorePValue = false;
		} else {
			useAbsoluteZScorePValue = useAbsPVal;
		}

		try {
			nrthread = config.getInteger("defaults.analysis.threads", null);
		} catch (Exception e) {
		}

		if (nrthread == null) {
			nrThreads = (Runtime.getRuntime().availableProcessors() - 1);
		} else {
			int numProcs = Runtime.getRuntime().availableProcessors();
			System.out.println("Machine has " + numProcs + " CPUs");

			boolean forcethreads = false;
			try {
				forcethreads = config.getBoolean("defaults.analysis.forcethreads", false);
			} catch (Exception e) {
			}

			if (forcethreads) {
				System.out.println("WARNING: forcing " + nrthread + " threads");
				nrThreads = nrthread;
			} else {
				if (nrthread > numProcs || nrthread < 1) {
					nrThreads = numProcs;
				} else {
					nrThreads = nrthread;
				}
			}


			System.out.println(nrThreads + " will be used for analysis.");


		}

		try {
			randomseed = config.getInteger("defaults.analysis.randomseed", null);
		} catch (Exception e) {
		}

		if (randomseed != null) {
			rSeed = randomseed;
			randomNumberGenerator = new Random(rSeed);
		}

		// multiple testing
		try {
			correctiontype = config.getString("defaults.multipletesting.type", null);
		} catch (Exception e) {
		}
		if (correctiontype != null) {
		} else {
		}

		// skipFDRCalculation
		try {
			skipFDRCalculation = config.getBoolean("defaults.multipletesting.skipFDRCalculation", false);
		} catch (Exception e) {
		}

		try {
			mtThreshold = config.getDouble("defaults.multipletesting.threshold", null);
		} catch (Exception e) {
		}

		if (mtThreshold != null) {
			fdrCutOff = mtThreshold;
		} else {
			fdrCutOff = 0.05;
		}

		try {
			numPermutations = config.getInteger("defaults.multipletesting.permutations", null);
		} catch (Exception e) {
		}
		if (numPermutations != null) {
			nrPermutationsFDR = numPermutations;
		}

		try {
			startWithPermutation = config.getInteger("defaults.multipletesting.startpermutation", null);
		} catch (Exception e) {
		}
		try {
			stopWithPermutation = config.getInteger("defaults.multipletesting.stoppermutation", null);
		} catch (Exception e) {
		}

		try {
			fdrtype = config.getString("defaults.multipletesting.fdrtype", "all");
			fdrtype = fdrtype.toLowerCase();
			fdrtype = fdrtype.replaceAll("-", "");
			fdrtype = fdrtype.replaceAll("level", "");
		} catch (Exception e) {
		}
		if (fdrtype != null) {
			if (fdrtype.equals("gene")) {
				fdrType = FDRMethod.GENELEVEL;
				createDotPlot = false;
			} else if (fdrtype.equals("probe")) {
				fdrType = FDRMethod.PROBELEVEL;
				createDotPlot = false;
			} else if (fdrtype.equals("snp")) {
				fdrType = FDRMethod.SNPLEVEL;
				createDotPlot = false;
			} else if (fdrtype.equals("snpprobe") || fdrtype.equals("full")) {
				fdrType = FDRMethod.FULL;
			}
		}

		try {
			largeFdrFileOut = config.getBoolean("defaults.multipletesting.fullFdrOutput", true);
		} catch (Exception e) {
		}
		if (largeFdrFileOut == false) {
			createDotPlot = false;
			fullFdrOutput = false;
		}


		// output settings
		try {
			outdir = config.getString("defaults.output.outputdirectory", null);
		} catch (Exception e) {
		}
		try {
			writeSNPQCLog = config.getBoolean("defaults.output.writeSNPQCLog", true);
		} catch (Exception e) {

		}

		try {
			dumpeverythingtodisk = config.getBoolean("defaults.output.dumpeverything", false);
		} catch (Exception e) {

		}

		try {
			omitDatasetSummaryStats = config.getBoolean("defaults.output.omitDatasetSummaryStats", false);
		} catch (Exception e) {

		}

		try {
			usemd5hash = config.getBoolean("defaults.output.usemd5hashforbinaryoutput", false);
		} catch (Exception e) {

		}

		if (outdir != null) {
			outputReportsDir = outdir;
			if (!outputReportsDir.endsWith(Gpio.getFileSeparator())) {
				outputReportsDir += Gpio.getFileSeparator();
			}

			// check if dir exists. if it does not, create it:
			if (!Gpio.exists(outdir)) {
				Gpio.createDir(outdir);
			}

			config.save(outputReportsDir + "metaqtlsettings.xml");

		} else {
			System.out.println("Error: please supply an output directory.");
			System.exit(-1);
		}

		try {
			createBinaryOutputFiles = config.getBoolean("defaults.output.binaryoutput", false);
			createBinaryFilesOnlyMetaAnalysis = config.getBoolean("defaults.output.binaryoutonlymetaanalysis", false);
			createTEXTOutputFiles = config.getBoolean("defaults.output.textoutput", true);
		} catch (Exception e) {
		}

		try {
			outputplotthreshold = config.getDouble("defaults.output.outputplotthreshold", -1);
		} catch (Exception e) {
		}

		if (outputplotthreshold != null) {
			plotOutputPValueCutOff = outputplotthreshold;
		} else {
			plotOutputPValueCutOff = -1;
		}

		try {
			outputplotdirectory = config.getString("defaults.output.outputplotdirectory", null);

		} catch (Exception e) {
		}

		if (outputplotdirectory != null) {
			plotOutputDirectory = outputplotdirectory;
			if (!plotOutputDirectory.endsWith(Gpio.getFileSeparator())) {
				plotOutputDirectory += Gpio.getFileSeparator();
			}
		} else {
			plotOutputDirectory = outdir + "/plots/";
		}

		// check if dir exists. if it does not, create it if plots are requested
		if (plotOutputPValueCutOff != 0) {
			if (!Gpio.exists(plotOutputDirectory)) {
				Gpio.createDir(plotOutputDirectory);
			}
		}

		try {
			createSNPPValueSummaryStatisticsFile = config.getBoolean("defaults.output.generatesnppvaluesummarystatistics", false);
		} catch (Exception e) {
		}

		try {
			provideFoldChangeData = config.getBoolean("defaults.output.generatefoldchangevalues", false);
		} catch (Exception e) {
		}

		try {
			provideBetasAndStandardErrors = config.getBoolean("defaults.output.generatebetaandfoldchanges", false);
		} catch (Exception e) {
		}

		try {
			createSNPSummaryStatisticsFile = config.getBoolean("defaults.output.generatesnpsummarystatistics", false);
		} catch (Exception e) {
		}

		try {
			createEQTLPValueTable = config.getBoolean("defaults.output.generateeqtlpvaluetable", false);
		} catch (Exception e) {
		}

		try {
			maxNrMostSignificantEQTLs = config.getInt("defaults.output.maxnreqtlresults", 150000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Load only expression probes that map to a known chromosome:
		try {
			expressionDataLoadOnlyProbesThatMapToChromosome = config.getBoolean("defaults.confine.confineProbesThatMapToKnownChromosome", false);
		} catch (Exception e) {
		}

		try {
			confineToProbesThatMapToChromosome = config.getByte("defaults.confine.confineToProbesThatMapToChromosome");
		} catch (Exception e) {
		}
		try {
			snpProbeConfineBasedOnChrPos = config.getBoolean("defaults.confine.snpProbeConfineBasedOnChrPos", false);
		} catch (Exception e) {

		}
		// confinements on snp, probe, or snp-probe
		String confineSNP = null;
		String confineProbe = null;
		String snpProbeConfine = null;

		confineSNPsToSNPsPresentInAllDatasets = null;

		// confine to this list of snp
		try {
			confineSNP = config.getString("defaults.confine.snp", null);

			if (confineSNP != null && confineSNP.length() > 0) {
				numberOfVariantsToBuffer = 1;
			}
		} catch (Exception e) {
		}


		// confine to this list of probes
		try {
			confineProbe = config.getString("defaults.confine.probe", null);

		} catch (Exception e) {
		}

		// confine to this list of snp-probe combinations
		try {
			snpProbeConfine = config.getString("defaults.confine.snpProbe", null);

		} catch (Exception e) {
		}

		try {
			snpProbeConfineBasedOnChrPos = config.getBoolean("defaults.confine.snpProbeConfineBasedOnChrPos", false);
		} catch (Exception e) {
		}

		if (confineSNP != null && confineSNP.trim().length() > 0 && Gpio.exists(confineSNP)) {
			strConfineSNP = confineSNP;
			TextFile in = new TextFile(confineSNP, TextFile.R);
			tsSNPsConfine = new HashSet<String>();
			String[] data = in.readAsArray();
			for (String d : data) {
				d = d.trim();
				if (d.length() > 0) {
					String[] elems = TAB_PATTERN.split(d);
					d = elems[0].trim();
					tsSNPsConfine.add(d.intern());
				}
			}
			in.close();
		} else if (confineSNP != null && confineSNP.trim().length() > 0 && !Gpio.exists(confineSNP)) {
			throw new IOException("Error! SNP confinement file: " + confineSNP + " could not be found.");
		}

		if (confineProbe != null && confineProbe.trim().length() > 0 && Gpio.exists(confineProbe)) {
			strConfineProbe = confineProbe;
			TextFile in = new TextFile(confineProbe, TextFile.R);
			tsProbesConfine = new THashSet<String>();
			String[] data = in.readAsArray();
			for (String d : data) {
				d = d.trim();
				while (d.startsWith(" ")) {
					d = d.substring(1);
				}
				tsProbesConfine.add(new String(d.getBytes("UTF-8")));
			}
			in.close();
		} else if (confineProbe != null && confineProbe.trim().length() > 0 && !Gpio.exists(confineProbe)) {
			throw new IOException("Error! Probe confinement file: " + confineProbe + " could not be found.");
		}

		if (snpProbeConfine != null && snpProbeConfine.trim().length() > 0 && Gpio.exists(snpProbeConfine)) {
			loadSNPProbeConfinement(snpProbeConfine);
		} else if (snpProbeConfine != null && snpProbeConfine.trim().length() > 0 && !Gpio.exists(snpProbeConfine)) {
			if (!new File(snpProbeConfine).exists()) {
				throw new IOException("Error! SNP-Probe confinement file: " + snpProbeConfine + " could not be found.");
			} else if (!new File(snpProbeConfine).canRead()) {
				throw new IOException("Error! SNP-Probe confinement file: " + snpProbeConfine + " could not be read.");
			} else {
				throw new IOException("Error! SNP-Probe confinement file: " + snpProbeConfine + " read error.");
			}
		}

		// confine to snp present in all datasets
		try {
			confineSNPsToSNPsPresentInAllDatasets = config.getBoolean("defaults.confine.confineSNPsToSNPsPresentInAllDatasets", false);
		} catch (Exception e) {
		}

		try {
			requireAtLeastNumberOfDatasets = config.getInt("defaults.output.requireAtLeastNumberOfDatasets", 1);
		} catch (Exception e) {
		}

		// confine to SNP that map to this chromosome
		try {
			String confineStr = config.getString("defaults.confine.confineToSNPsThatMapToChromosome", null);
			if (confineStr == null || confineStr.trim().length() == 0) {
				confineToSNPsThatMapToChromosome = null;
			} else {

				confineToSNPsThatMapToChromosome = ChrAnnotation.parseChr(confineStr);
				if (confineToSNPsThatMapToChromosome < 1) {
					confineToSNPsThatMapToChromosome = null;
				}
			}

		} catch (Exception e) {
		}

		// confine to probes present in all datasets
		confineProbesToProbesPresentInAllDatasets = false;
		try {
			confineProbesToProbesPresentInAllDatasets = config.getBoolean("defaults.confine.confineToProbesPresentInAllDatasets", false);
		} catch (Exception e) {
		}

		try {
			String batchsizestr = config.getString("defaults.analysis.batchsize", null);
			String batchidstr = config.getString("defaults.analysis.batchid", null);
			if (batchidstr != null && batchsizestr != null) {
				this.batchid = Integer.parseInt(batchidstr);
				this.batchsize = Integer.parseInt(batchsizestr);
			}
		} catch (Exception e) {

		}
		try {
			String dpwstr = config.getString("defaults.analysis.displayWarnings", null);
			boolean b = Boolean.parseBoolean(dpwstr);
			displayWarnings = b;
		} catch (Exception e) {

		}


		regressOutEQTLEffectFileName = null;
		try {
			regressOutEQTLEffectFileName = config.getString("defaults.analysis.regressOutEQTLEffects", null);
			if (regressOutEQTLEffectFileName.equals("")) {
				regressOutEQTLEffectFileName = null;
			}
		} catch (Exception e) {
		}
		regressOutEQTLEffectsSaveOutput = false;
		try {
			regressOutEQTLEffectsSaveOutput = config.getBoolean("defaults.analysis.regressOutEQTLEffectsSaveOutput", false);
		} catch (Exception e) {
		}

		// is there a pathway definition?
		String pathwayDef = null;
		try {
			pathwayDef = config.getString("defaults.analysis.pathwaydefinition", null);
		} catch (Exception e) {
		}
		if (pathwayDef != null && !pathwayDef.equals("")) {
			this.pathwayDefinition = pathwayDef;
		}

		// dataset parameters
		int i = 0;

		String dataset = config.getString("datasets.dataset(" + i + ").name", null);  // see if a dataset is defined


		datasetSettings = new ArrayList<TriTyperGeneticalGenomicsDatasetSettings>();


//            ArrayList<GeneticalGenomicsDataset> vGG = new ArrayList<GeneticalGenomicsDataset>();                    // create a dataset vector
//            GeneticalGenomicsDataset tmpDataset;                                               // create a temporary dataset object
		while (dataset != null) {

			String expressionData = null;
			String dataloc = null;
			String genToExpCoupling = null;
			Boolean qnorm = false;
			Boolean logtr = false;

			TriTyperGeneticalGenomicsDatasetSettings s = new TriTyperGeneticalGenomicsDatasetSettings();
			s.name = dataset;
			datasetSettings.add(s);

			s.cisAnalysis = this.cisAnalysis;
			s.transAnalysis = this.transAnalysis;
			// get the location of the expression data
			try {
				expressionData = config.getString("datasets.dataset(" + i + ").expressiondata", null);

			} catch (Exception e) {
			}

			String expressionPlatform = null;
			try {
				expressionPlatform = config.getString("datasets.dataset(" + i + ").expressionplatform", null);

			} catch (Exception e) {
			}

			probeannotation = null;
			try {
				probeannotation = config.getString("datasets.dataset(" + i + ").probeannotation", null);

				if (probeannotation.length() == 0) {
					probeannotation = null;
				}
			} catch (Exception e) {
			}

			s.expressionplatform = expressionPlatform;
			s.probeannotation = probeannotation;
			s.expressionLocation = expressionData;

			// get the location of the dataset
			try {
				dataloc = config.getString("datasets.dataset(" + i + ").location", null);

			} catch (Exception e) {
				System.out.println("Please provide a location on your disk where " + dataset + " is located");
				System.exit(-1);
			}

			Gpio.formatAsDirectory(dataloc);

			s.genotypeLocation = dataloc;

			try {
				s.snpFileLocation = config.getString("datasets.dataset(" + i + ").snps", null);
			} catch (Exception e) {

			}

			try {
				s.snpmapFileLocation = config.getString("datasets.dataset(" + i + ").snpmappings", null);
			} catch (Exception e) {

			}

			// see if there are covariates to load
			String covariateFile = null;
			try {
				covariateFile = config.getString("datasets.dataset(" + i + ").covariates", null);
				if (covariateFile.equals("")) {
					covariateFile = null;
				}

			} catch (Exception e) {
			}

			s.covariateFile = covariateFile;

			// see if there is a genotype to expression couplings file
			try {
				genToExpCoupling = config.getString("datasets.dataset(" + i + ").genometoexpressioncoupling", null);

			} catch (Exception e) {
			}

			s.genotypeToExpressionCoupling = genToExpCoupling;

			// quantile normalize the expression data?
			try {
				qnorm = config.getBoolean("datasets.dataset(" + i + ").quantilenormalize", false);
			} catch (Exception e) {
			}

			s.quantilenormalize = qnorm;

//                if (qnorm) {
//                    tmpDataset.quantileNormalize();
//                }
//
			// log2 transform the expression data?
			try {
				logtr = config.getBoolean("datasets.dataset(" + i + ").logtranform", false);
			} catch (Exception e) {
			}
			s.logtransform = logtr;

			dataset = null;
			i++;
			try {
				dataset = config.getString("datasets.dataset(" + i + ").name", null);

			} catch (Exception e) {
			}

			s.confineProbesToProbesMappingToAnyChromosome = confineProbesToProbesMappingToAnyChromosome;
			s.confineProbesToProbesThatMapToChromosome = confineProbesToProbesThatMapToChromosome;
			s.tsProbesConfine = tsProbesConfine;

		}

		if (datasetSettings.isEmpty()) {
			System.out.println("Error: no datasets defined");
			System.exit(-1);
		} else if (requireAtLeastNumberOfDatasets > datasetSettings.size()) {
			System.out.println("Error: requireAtLeastNumberOfDatasets is larger than number of loaded datasets");
			System.exit(-1);
		}

		// summarize();

	}

	public String summarize() {
		Date currentDataTime = new Date();
		String summary = "QTL mapping was performed using metaqtl version: " + Main.VERSION + "\nCurrent date and time: " + Main.DATE_TIME_FORMAT.format(currentDataTime) + "\n\n"
				+ "Following settings will be applied:\n"
				+ "Settings\n----\n"
				+ "settingsTextToReplace\t" + settingsTextToReplace + "\n"
				+ "settingsTextReplaceWith\t" + settingsTextReplaceWith + "\n"

				+ "\nOutput\n----\n"
				+ "createSNPPValueSummaryStatisticsFile\t" + createSNPPValueSummaryStatisticsFile + "\n"
				+ "createEQTLPValueTable\t" + createEQTLPValueTable + "\n"
				+ "outputReportsDir\t" + outputReportsDir + "\n"
				+ "\nAnalysis\n----\n"
				+ "randomseed\t" + rSeed + "\n"
				+ "performCiseQTLAnalysis\t" + cisAnalysis + "\n"
				+ "performTranseQTLAnalysis\t" + transAnalysis + "\n"
				+ "performParametricAnalysis\t" + performParametricAnalysis + "\n"
				+ "useAbsoluteZScorePValue\t" + useAbsoluteZScorePValue + "\n"
				+ "ciseQTLAnalysMaxSNPProbeMidPointDistance\t" + ciseQTLAnalysMaxSNPProbeMidPointDistance + "\n"
				+ "maxNrMostSignificantEQTLs\t" + maxNrMostSignificantEQTLs + "\n"
				+ "performParametricAnalysisGetAccuratePValueEstimates\t" + performParametricAnalysisGetAccuratePValueEstimates + "\n"
				+ "nrThreads\t" + nrThreads + "\n"
				+ "fdrCutOff\t" + fdrCutOff + "\n"
				+ "fdrType\t" + fdrType + "\n"
				+ "nrPermutationsFDR\t" + nrPermutationsFDR + "\n"
				+ "regressOutEQTLEffectFileName\t" + regressOutEQTLEffectFileName + "\n"
				+ "snpQCCallRateThreshold\t" + snpQCCallRateThreshold + "\n"
				+ "snpQCHWEThreshold\t" + snpQCHWEThreshold + "\n"
				+ "snpQCMAFThreshold\t" + snpQCMAFThreshold + "\n"
				+ "\nConfinements\n----\n"
				+ "performEQTLAnalysisOnSNPProbeCombinationSubset\t" + performEQTLAnalysisOnSNPProbeCombinationSubset + "\n"
				+ "confineToSNPsThatMapToChromosome\t" + confineToSNPsThatMapToChromosome + "\n"
				+ "expressionDataLoadOnlyProbesThatMapToChromosome\t" + expressionDataLoadOnlyProbesThatMapToChromosome + "\n"
				//                + "tsSNPsConfine\t" +tsSNPsConfine+ "\n"
				//                + "tsProbesConfine\t" +tsProbesConfine+ "\n"
				//                + "tsSNPProbeCombinationsConfine\t" +tsSNPProbeCombinationsConfine+ "\n"
				+ "confineSNPsToSNPsPresentInAllDatasets\t" + confineSNPsToSNPsPresentInAllDatasets + "\n"
				+ "confineProbesToProbesPresentInAllDatasets\t" + confineProbesToProbesPresentInAllDatasets + "\n"
				+ "confineToProbesThatMapToChromosome\t" + confineToProbesThatMapToChromosome + "\n"
				+ "expressionDataLoadOnlyProbesThatMapToChromosome\t" + expressionDataLoadOnlyProbesThatMapToChromosome + "\n"
				+ "\n";

		if (tsProbesConfine != null) {
			summary += "Confining to: " + tsProbesConfine.size() + " probes from: " + strConfineProbe + "\n";
		}

		if (tsSNPProbeCombinationsConfine != null) {
			summary += "Confining to: " + tsSNPProbeCombinationsConfine.size() + " SNP-probe combinations from: " + strConfineSNPProbe + "\n";
		}

		if (tsSNPsConfine != null) {
			summary += "Confining to: " + tsSNPsConfine.size() + " SNPs  from: " + strConfineSNP + "\n";
		}

		summary += "\nDatasets\n----\n";

		for (TriTyperGeneticalGenomicsDatasetSettings settings : this.datasetSettings) {
			summary += "DatasetName\t" + settings.name + "\n";
			summary += "ExpressionData\t" + settings.expressionLocation + "\n";
			summary += "ExpressionPlatform\t" + settings.expressionplatform + "\n";
			summary += "LogTransform\t" + settings.logtransform + "\n";
			summary += "QuantileNormalize\t" + settings.quantilenormalize + "\n";
			summary += "GenotypeData\t" + settings.genotypeLocation + "\n";
			summary += "GTE\t" + settings.genotypeToExpressionCoupling + "\n";
			summary += "ProbaAnnotation\t" + settings.probeannotation + "\n";

		}

		return summary;

	}

	public void writeSettingsToDisk() throws Exception {
		TextFile tf = new TextFile(this.outputReportsDir + "/UsedSettings.txt", TextFile.W);
		tf.write(summarize());
		tf.close();
	}

	public void loadSNPProbeConfinement(String snpProbeConfine) throws IOException {
		strConfineSNPProbe = snpProbeConfine;
		TextFile in = new TextFile(snpProbeConfine, TextFile.R);
		tsSNPProbeCombinationsConfine = new HashMap<String, HashSet<String>>();
		String[] elems = in.readLineElemsReturnReference(TextFile.tab);
		System.out.println("Loading SNP Probe pairs from: " + snpProbeConfine);
		while (elems != null) {

			if (elems.length > 1) {

				String snp = new String(elems[0].getBytes("UTF-8"));
				snp = snp.trim();
				while (snp.startsWith(" ")) {
					snp = snp.substring(1);
				}
				if ((tsSNPsConfine != null && tsSNPsConfine.contains(snp)) || tsSNPsConfine == null) {
					HashSet<String> probes = tsSNPProbeCombinationsConfine.get(snp);
					if (probes == null) {
						probes = new HashSet<String>();
					}
					String probe = new String(elems[1].getBytes("UTF-8"));
					probe = probe.trim();
					while (probe.startsWith(" ")) {
						probe = probe.substring(1);
					}
					if ((tsProbesConfine != null && tsProbesConfine.contains(probe)) || tsProbesConfine == null) {
						probes.add(probe);
					}
					tsSNPProbeCombinationsConfine.put(snp, probes);
				}
			}
			elems = in.readLineElemsReturnReference(TextFile.tab);
		}
		in.close();
	}
}
