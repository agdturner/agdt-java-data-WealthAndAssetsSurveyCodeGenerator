# agdt-java-generic-data-WealthAndAssetsSurveyCodeGenerator
https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurveyCodeGenerator
A Java library for parsing Office of National Statistics Wealth and Assets Survey data to generate Java source code for loading that data.
The resulting Java source code forms part of the agdt-java-generic-data-WealthAndAssetsSurvey Java library which can be found via:
https://github.com/agdturner/agdt-java-generic-data-WealthAndAssetsSurvey/

The Office of National Statistics Wealth and Assets Survey (WaAS) data is described on the ONS Website:
https://www.ons.gov.uk/peoplepopulationandcommunity/personalandhouseholdfinances/debt/methodologies/wealthandassetssurveyqmi

Waves 1 to 5 of the Survey are available to UK academics via the UKDA:
https://beta.ukdataservice.ac.uk/datacatalogue/studies/study?id=7215
Wave 6 is due for release in 2019.

Each wave of the data has two files:
1. A household which contains variables about a household.
2. A person file with details of some persons within each household.

These files have quite a lot of variables and there are some subtle changes to these over time.

This library allows for the data to be automatically parsed to discover what the common variables in the files are and what data types can best represent the values of these variables. It then produces some Java source code that makes it easy to load and process these data.

Some of the Java code generation methods are generic and there is a plan to abstract these to the following library:
https://github.com/agdturner/agdt-java-generic-CodeGenerator

## Dependencies
Please see the pom.xml for details.
