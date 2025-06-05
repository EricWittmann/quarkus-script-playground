package io.quarkiverse.quickjs4j.deployment;

import org.jboss.jandex.DotName;

public class TestIt {

    public static void main(String[] args) {
        DotName simple = DotName.createSimple("io.quarkiverse.quickjs4j.deployment.TestIt");
        System.out.println(simple.packagePrefix());
        System.out.println(simple.packagePrefixName());
        System.out.println(simple.withoutPackagePrefix());
    }

}
