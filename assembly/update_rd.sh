#!/bin/bash                                                                                                                                                                         
snapshot="false"                                                                                                                                                                    
descriptor=./target/release-descriptor/releaseDescriptor.xml                                                                                                                        
version=$(xmllint --pretty 2 ./target/release-descriptor/releaseDescriptor.xml | grep version | sed -n '2p' | sed  's/\s//g'| sed -n 's/version="\(.*\)"/\1/p')                     
                                                                                                                                                                                    
echo "Checking out gh-pages"                                                                                                                                                        
git checkout gh-pages                                                                                                                                                               
git checkout -b rd-$version                                                                                                                                                         
if [ "x$(echo $version | grep SNAPSHOT)" == "x"  ]; then                                                                                                                            
        echo "Creating release descriptor"                                                                                                                                          
        cp  $descriptor ./releases/latest                                                                                                                                           
else                                                                                                                                                                                
        echo "Creating snapshot descriptor"                                                                                                                                         
        cp  $descriptor ./releases/snapshot                                                                                                                                         
fi                                                                                                                                                                                  
cp $descriptor releases/$version                                                                                                                                                    
echo "Commiting changes"                                                                                                                                                            
git add -A                                                                                                                                                                          
git commit -m "Add release descriptors for $version"                                                                                                                                
git push -u origin rd-$version                                                                                                                                                      
echo "Please, remember to merge rd-$version into gh-pages"                                                                                                                          
git checkout master                                                                                                                                                                 
                                                                                                                                                                                    

