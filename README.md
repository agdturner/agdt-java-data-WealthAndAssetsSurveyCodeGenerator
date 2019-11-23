# Wealth and Assets Survey Java Code Generation Library
https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurveyCodeGenerator

A Java library for parsing Office of National Statistics Wealth and Assets Survey (WaAS) data to generate Java source code for loading that data.

The generated Java source code forms part of the agdt-java-generic-data-WealthAndAssetsSurvey Java library - a distinct library for processing this data which can be found via:
https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurvey/
Specifically, this is the code found in the following packages:
1. https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurvey/tree/master/src/main/java/uk/ac/leeds/ccg/andyt/generic/data/waas/data/hhold
2. https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurvey/tree/master/src/main/java/uk/ac/leeds/ccg/andyt/generic/data/waas/data/person

The Office of National Statistics Wealth and Assets Survey (WaAS) data are described on the ONS Website:
https://www.ons.gov.uk/peoplepopulationandcommunity/personalandhouseholdfinances/debt/methodologies/wealthandassetssurveyqmi

Waves 1 to 5 of the Survey are available for academic research via the UKDS:
https://beta.ukdataservice.ac.uk/datacatalogue/studies/study?id=7215
Wave 6 is due for release in 2019.

Each wave of the data has two files:
1. A household file which contains variables about each household in the survey.
2. A person file which contains variables bout some persons within each household in the survey.

Each type of file has quite a lot of variables and there are some subtle changes in variables between waves.

This library is for automatically parsing the WaAS data to discover what the common variables in the files across the waves are and what data types can best represent the values of these variables. It produces some Java source code that makes it easy to load and process these data. The generated Java source code are added to another library. This library was separated from that other library due to both complications in compiling the source code and to make it easier to abstract and generalise the code specifically for generating Java source code.

Some of the Java code generation methods are generic and are being abstracted to the following library:
https://github.com/agdturner/agdt-java-generic-CodeGenerator

## Dependencies
Please see the pom.xml for details.

## Contributions
Please raise issues and submit pull requests in the usual way. Contributions will be acknowledged. 

## LICENCE
Please see the standard Apache 2.0 open source LICENCE.
