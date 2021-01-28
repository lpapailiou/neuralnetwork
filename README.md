# neural network library
This is a library for a neural network in java 8.  
The library can be used both for supervised and unsupervised machine learning approaches.  
The network architecture is freely configurable.  

## Genetic Algorithm
The package ``geneticalgorithm`` introduced in version 2.0 will allow easy implementation of the
genetic algorithm with reinforced learning. If it is used, please make sure to load the
``neuralnetwork.properties`` file to the resources folder of your project (see below).

### From a Jar file
You can download the Jar file directly from the [latest release](https://github.com/lpapailiou/neuralnetwork/releases/latest). Alternatively, you can build it yourself.  
Just add the jar as external library.
  
### From a Maven dependency  
Add following snippets to your ``pom.xml`` file:

    <repositories>    
        <repository>    
            <id>neuralnetwork</id>    
            <url>https://github.com/lpapailiou/neuralnetwork/raw/master</url>    
        </repository>    
    </repositories>      
  
    <dependencies>    
        <dependency>    
            <groupId>neuralnetwork</groupId>    
            <artifactId>neural-network-repo</artifactId>    
            <version>2.0</version>    
        </dependency>    
    </dependencies>    
    
Please note the `neuralnetwork.properties` file should be imported as well to your resources folder.  
In case it is not present, you may include following plugin to your pom.xml file:  

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.1</version>
        <executions>
            <execution>
                <id>resource-dependencies</id>
                <phase>compile</phase>
                <goals>
                    <goal>unpack</goal>
                </goals>
                <configuration>
                    <artifactItems>
                        <artifactItem>
                            <groupId>neuralnetwork</groupId>
                            <artifactId>neural-network-repo</artifactId>
                            <version>${neuralnetwork.version}</version>
                            <type>jar</type>
                            <overWrite>true</overWrite>
                        </artifactItem>
                    </artifactItems>
                    <includes>**/*.properties</includes>
                    <outputDirectory>${project.basedir}/src/main/resources</outputDirectory>
                </configuration>
            </execution>
        </executions>
    </plugin>

and run `mvn build` to complete the import of the properties file.