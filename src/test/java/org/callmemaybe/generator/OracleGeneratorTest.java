package org.callmemaybe.generator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.callmemaybe.CallMeMaybe;
import org.junit.Test;

public class OracleGeneratorTest {

  @Test
  public void oracleGeneratorTest1() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.ArrayListMultimap",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  @Test
  public void oracleGeneratorTest2() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.base.Converter",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  // for equivalences
  @Test
  public void oracleGeneratorTest3() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.base.Splitter",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true",
          "--cross-oracles",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  // for equivalences w/ clones
  @Test
  public void oracleGeneratorTest4() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.cache.LongAdder",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true",
          "--cross-oracles",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  @Test
  public void oracleGeneratorTestSnippets() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.Multimap",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true",
          "--cross-oracles",
          "true",
          "--test-class",
          "classe.esempio.uno:classe.esempio.due"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }
  // for equivalences in interfaces
  @Test
  public void oracleGeneratorTestInterface() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.Multiset",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true",
          "--cross-oracles",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  // for equivalences w/ varargs
  @Test
  public void oracleGeneratorTestVarArgs() throws Exception {
    CallMeMaybe.main(
        new String[] {
          "--target-class",
          "com.google.common.collect.Iterables",
          "--class-dir",
          "src/test/resources/bin/guava-19.0.jar",
          "--source-dir",
          "src/test/resources/src/guava-19.0-sources",
          "--oracle-generation",
          "true",
          "--cross-oracles",
          "true"
        });

    String outputDir = CallMeMaybe.configuration.getAspectsOutputDir();
    File actualOutput = Paths.get(outputDir, "Aspect_1.java").toFile();
    File expectedOutput =
        Paths.get(getClass().getClassLoader().getResource("aspects/Aspect_1.java").toURI())
            .toFile();
    assertThat(FileUtils.contentEquals(actualOutput, expectedOutput), is(true));
  }

  //  @After
  //  public void deleteToradocuOutputDir() {
  //    FileUtils.deleteQuietly(new File(CallMeMaybe.configuration.getAspectsOutputDir()));
  //  }

}
