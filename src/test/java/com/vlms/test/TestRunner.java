package com.vlms.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("                   VLMS JUNIT TEST SUITE SIMULATOR                        ");
        System.out.println("==========================================================================");

        List<Class<?>> testClasses = new ArrayList<>();
        testClasses.add(com.vlms.workflow.ChainOfResponsibilityTest.class);
        testClasses.add(com.vlms.workflow.BusinessRulesTest.class);
        testClasses.add(com.vlms.workflow.VendorBuilderTest.class);
        testClasses.add(com.vlms.workflow.TaskServiceTest.class);
        testClasses.add(com.vlms.workflow.WorkflowEngineTest.class);

        int totalRan = 0;
        int totalPassed = 0;
        int totalFailed = 0;

        for (Class<?> testClass : testClasses) {
            System.out.println("\nRunning tests in: " + testClass.getSimpleName());
            System.out.println("--------------------------------------------------------------------------");

            Method beforeEachMethod = null;
            List<Method> testMethods = new ArrayList<>();

            for (Method method : testClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(BeforeEach.class)) {
                    beforeEachMethod = method;
                } else if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                }
            }

            for (Method testMethod : testMethods) {
                totalRan++;
                try {
                    Object testInstance = testClass.getDeclaredConstructor().newInstance();
                    
                    if (beforeEachMethod != null) {
                        beforeEachMethod.setAccessible(true);
                        beforeEachMethod.invoke(testInstance);
                    }

                    testMethod.setAccessible(true);
                    testMethod.invoke(testInstance);
                    System.out.println("  [PASSED] " + testMethod.getName());
                    totalPassed++;
                } catch (Throwable t) {
                    System.out.println("  [FAILED] " + testMethod.getName());
                    Throwable cause = t.getCause() != null ? t.getCause() : t;
                    cause.printStackTrace(System.out);
                    totalFailed++;
                }
            }
        }

        System.out.println("\n==========================================================================");
        System.out.println("                            TEST SUMMARY                                  ");
        System.out.println("==========================================================================");
        System.out.println("  Total Tests Run: " + totalRan);
        System.out.println("  Passed:          " + totalPassed + " (" + String.format("%.1f", ((double) totalPassed / totalRan) * 100.0) + "%)");
        System.out.println("  Failed:          " + totalFailed);
        System.out.println("==========================================================================");

        if (totalFailed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
