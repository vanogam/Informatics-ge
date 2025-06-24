if [ -z "$1" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

VERSION=$1

docker build -t informatics/core:$VERSION -f docker/main/Dockerfile .
docker build -t informatics/ui:$VERSION -f informatics-ui/Dockerfile ./informatics-ui
docker build -t informatics/worker:$VERSION -f informatics-worker/Dockerfile ./informatics-worker

echo "Docker images built and pushed with version: $VERSION"