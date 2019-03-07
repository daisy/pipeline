# ssh-keygen -t rsa -b 4096 -C 'noreply@travis-ci.org' -f .travis/secrets/deploy_key
# travis encrypt-file --repo daisy/pipeline .travis/secrets/deploy_key
# mv deploy_key.enc .travis/secrets
# rm .travis/secrets/deploy_key
# rm .travis/secrets/deploy_key.pub
# git add .travis/secrets/deploy_key.enc

if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_BRANCH" != "website" ]; then
    echo "Skipping deployment"
    exit 0
else
    openssl aes-256-cbc -K $encrypted_0ca289d261f6_key -iv $encrypted_0ca289d261f6_iv \
                        -in .travis/secrets/deploy_key.enc -out .travis/secrets/deploy_key -d && \
    chmod 600 .travis/secrets/deploy_key && \
    eval `ssh-agent -s` && \
    ssh-add .travis/secrets/deploy_key && \
    git config --global user.name "travis-ci" && \
    git config --global user.email "noreply@travis-ci.org" && \
    make publish
    make publish-wiki
fi
