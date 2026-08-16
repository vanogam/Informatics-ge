if [ -z "$1" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

VERSION=$1

docker build -t informatics/core:$VERSION -f docker/main/Dockerfile .
if [ "$2" == "--dev" ]; then
  echo "Skipping UI build due to --dev flag"
else
  docker build -t informatics/ui:$VERSION -f informatics-ui/Dockerfile ./informatics-ui
fi
docker build -t informatics/worker:$VERSION -t informatics/worker:latest -f informatics-worker/Dockerfile ./informatics-worker
# Note: sandbox:latest is built by dockerfile-maven-plugin during `mvn install` in
# informatics-worker, so it is not rebuilt here. deploy/deploy.sh builds the versioned,
# pushable copy.

echo "Docker images built locally with version: $VERSION (run deploy/deploy.sh to push)"