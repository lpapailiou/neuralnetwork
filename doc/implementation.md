# Implementation
See the implementation instructions below.  
Note that the ui components rely on ```javafx```, so ``java 8`` is required.

## Table of Contents
1. [From jar file](#from-jar-file)  
2. [As maven dependency](#as-maven-dependency)  
3. [Properties](#properties)  

## From jar file
You can download the Jar file directly from the [latest release](https://github.com/lpapailiou/neuralnetwork/releases/latest). Alternatively, you can build it yourself.  
Just add the jar as external library. Be sure to copy the ``neuralnetwork.properties`` file to your `resources` folder.

## As maven dependency
Add following snippets to your ``pom.xml`` file to import the library:

    <properties>
        <!-- base properties -->
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            
        <!-- paths -->
        <main.class>your.main.class</main.class>                            <!-- change this line -->
        <outputDir>target/classes</outputDir>
        <resourceDir>src/main/resources</resourceDir>

        <!-- plugin handling -->
        <maven-dependency-plugin.version>3.2.0</maven-dependency-plugin.version>
        <maven-assembly-plugin.version>3.1.0</maven-assembly-plugin.version>
        <neuralnetwork.group>ch.kaiki.nn</neuralnetwork.group>
        <neuralnetwork.id>neural-network</neuralnetwork.id>        
        <neuralnetwork.version>3.0</neuralnetwork.version>                  <!-- fixed version -->
        <!-- <neuralnetwork.version>LATEST</neuralnetwork.version> -->      <!-- latest version -->
    </properties>

    <repositories>    
        <!-- download url to sync with online repository -->
        <repository>    
            <id>neuralnetwork</id>    
            <url>https://github.com/lpapailiou/neuralnetwork/raw/master</url>    
        </repository>    
    </repositories>      

    <dependencies>    
        <!-- maven dependency connecting to .m2 local repository -->
        <dependency>
            <groupId>${neuralnetwork.group}</groupId>
            <artifactId>${neuralnetwork.id}</artifactId>
            <version>${neuralnetwork.version}</version>
        </dependency> 
    </dependencies>    

Please note the `neuralnetwork.properties` file should be imported as well to your `resources` folder.  
In case it is not present, you may include following plugin to your `pom.xml` file:

    <build>
        <outputDirectory>${outputDir}</outputDirectory>
        <resources><resource><directory>${resourceDir}</directory></resource></resources>
        <plugins>
            <!-- copies properties file from neural network library to resources folder -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>${maven-dependency-plugin.version}</version>
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
                                    <groupId>${neuralnetwork.group}</groupId>
                                    <artifactId>${neuralnetwork.id}</artifactId>
                                    <version>${neuralnetwork.version}</version>
                                    <type>jar</type>
                                    <overWrite>true</overWrite>
                                </artifactItem>
                            </artifactItems>
                            <includes>**/*.properties</includes>
                            <outputDirectory>${project.basedir}/${resourceDir}</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- creates executable jar file with dependencies -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>${maven-assembly-plugin.version}</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>${main.class}</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

and run `mvn compile` to complete the import of the properties file.

## Properties
Below an overview of the `neuralnetwork.properties` file.

    # ************************************************************************************************************ #
    # ***********                                  COMMON PROPERTIES                                   *********** #
    # ************************************************************************************************************ #
    # the initializer function to initialize neural network matrices.
    # available values: static|random|xavier|kaiming
    initializer=random
    # the learning rate must have a value between 0.0 and 1.0.
    learning_rate=0.8
    # the default rectifier as activation function for the neural network.
    # available values: identity|relu|leaky_relu|sigmoid|sigmoid_accurate|silu|silu_accurate|tanh|elu|gelu|softplus|softmax.
    rectifier=sigmoid
    # the optimizer for the learning rate between iterations.
    # available values: none|sgd
    learning_rate_optimizer=sgd
    # the learning rate decay as momentum.
    # if learning_rate_optimizer is set to 'none', this value will have no effect.
    # must have a value between 0.0 and 1.0.
    learning_rate_momentum=0.01
    
    # ************************************************************************************************************ #
    # ***********                               SUPERVISED LEARNING ONLY                               *********** #
    # ************************************************************************************************************ #
    # the cost function is the metric for the error of one iteration. Its derivation is the loss.
    # available values: mse_naive|mse|cross_entropy|exponential|hellinger_distance|kld|gkld|isd
    cost_function=mse
    # the regularizer will adapt the penalty by the loss function.
    # available values: none|l1|l2|elastic
    regularizer=none
    # regularization functions may rely on a regularization parameter. this parameter, usually called lambda,
    # will be controlled by the property regularizer_param.
    regularizer_param=0
    # dropout is an additional regularization technique. during training, it will set a certain percentage
    # of output layer weights to zero and scale the output values by that factor.
    # during testing, dropout is not active, the output will be scaled up instead.
    dropout_factor=0
    # batch mode decides if gradients are summed up or the mean is used for backpropagation.
    # available values: mean|sum
    batch_mode=mean
    
    # ************************************************************************************************************ #
    # ***********                                GENETIC ALGORITHM ONLY                                *********** #
    # ************************************************************************************************************ #
    # the reproduction specimen count is the count of NeuralNetworks chosen for reproduction to be seeded to
    # the new generation to come.
    genetic_reproduction_specimen_count=1
    # the selection pool size for genetic evolution as a percentage of the best performing neural networks. fallback will be 1.
    genetic_reproduction_pool_size=0.5
    # the mutation rate is the percentage of the mutated components of the neural network matrices.
    # must have a value between 0.0 and 1.0
    mutation_rate=0.5
    # the optimizer for the mutation rate between iterations.
    # available values: none|sgd
    mutation_rate_optimizer=sgd
    # the mutation rate decay as momentum.
    # if mutation_rate_optimizer is set to 'none', this value will have no effect.
    # must have a value between 0.0 and 1.0.
    mutation_rate_momentum=0.01

