package com.j9soft.v1repository.entityrequests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
 
@RunWith( Cucumber.class )
@CucumberOptions(strict = true, features = "src/test/resources")
public class RunCucumberIT {

}