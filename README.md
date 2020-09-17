# neural network library
This is a library for a neural network in java 8.  
The library can be used both for supervised and unsupervised machine learning approaches.  
The network architecture is freely configurable.  

### From a Jar file
You can download the Jar file directly from the [latest release](https://github.com/lpapailiou/neuralnetwork/releases/latest). Alternatively, you can build it yourself.  
Just add the jar as external library.
  
### From a Maven dependency  
Add following snippets to your ``pom.xml`` file:

``<repositories>    
    <repository>    
        <id>neuralnetwork</id>    
        <url>https://github.com/lpapailiou/neuralnetwork/raw/master</url>    
    </repository>    
</repositories>``      
  
``<dependencies>    
    <dependency>    
        <groupId>neuralnetwork</groupId>    
        <artifactId>maven-repo</artifactId>    
        <version>1.1</version>    
    </dependency>    
</dependencies>``    