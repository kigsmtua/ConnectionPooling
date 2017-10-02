# ConnectionPooling
Connection pooling on a mysql database in java (Future versions will aim to make it generic to support major databases)

# How to use
```java
   import com.pooling.connectionpooling.Pooling
   
   
   class TestConnection(){
   
      private transient pooling;
      
      
      public TestConnection(){
          
          try(pooling = new Pooling('host','port','dbname','dbusername','dbpassword','poolname',poolsize)){
              
              //Do some other things you want to do at this point
            
          }catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | SQLException ex) {
                
                ///Maybe some logs at this point
                ///Whatever have you here
                System.exit(1);
               
               
          }
   
      }
      
   }
   ```
   
   And in the spirit of reusing the pool , just ensure the class is instantiated only once
