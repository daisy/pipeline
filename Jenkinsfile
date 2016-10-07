node {
    stage 'Checkout'
    checkout scm
    
    stage 'Build'
    sh 'mkdir -p .maven-cache'
    sh 'make dist-zip'
    
    stage 'Test'
    sh 'mkdir -p .maven-cache'
    sh 'make check'
}
