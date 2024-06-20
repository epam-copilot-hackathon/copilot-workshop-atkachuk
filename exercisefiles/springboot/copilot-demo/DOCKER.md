# Docker Commands for the Application

## Building the Docker Image

To build the Docker image, navigate to the directory containing the Dockerfile and run the following command:

```bash
docker build -t copilot-demo:0.0.1 .

docker run -p 8080:8080 copilot-demo:0.0.1

curl http://localhost:8080/health