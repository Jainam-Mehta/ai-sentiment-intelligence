AI Sentiment Intelligence
A complete sentiment analysis platform that monitors social media posts in real time.
It consists of a Java web appliormer model.cation(frontend + backend) and a Python AI service that performs sentiment analysis using a transf

Live Demo

Java app(deployed on Render): [https://sentiment-java.onrender.com](https://ai-sentiment-intelligence.onrender.com)

AI service(deployed on Hugging Face Spaces): [https://jainam-mehta-ai-sentiment-service.hf.space](https://huggingface.co/spaces/jainam-mehta/ai-sentiment-service/tree/main)

Features
Real‑time sentiment analysis (positive / neutral / negative)

Simulated and Reddit live data sources

User registration and login with session management

Private, guest & public monitoring modes

CSV upload for bulk analysis

Email reports (daily, weekly, monthly)

Dashboard with charts (pie chart & trend line)

Multi‑tenant support (private streams per user)

Tech Stack
Component	Technology
Java Web App	Java 17, Jakarta Servlets, JSP, Tomcat 10.1
AI Service	Python 3.11, Flask, Hugging Face Transformers (nlptown/bert-base-multilingual-uncased-sentiment)
Database	MySQL (Clever Cloud add‑on)
Deployment (Java)	Docker + Render
Deployment (AI)	Hugging Face Spaces
Build Tool	Maven

Prerequisites
Java 17 (JDK) (version 17 or higher)

Maven (for building the Java app)

Python 3.11 (for the AI service)

MySQL (local or remote)

Git

Docker (optional, for local container testing)

A Gmail account with 2FA enabled (if you want to test email reports)

Local Development Setup
1. Clone the repository
bash
git clone https://github.com/Jainam-Mehta/ai-sentiment-intelligence.git
cd ai-sentiment-intelligence
2. Set up the MySQL database
Create a database

Run the SQL script located in MySQL_WorkBench_Files/sentiment_dashboard.sql to create the tables and the stored procedure.

Alternatively, use the provided SQL script from the repository.

3. Run the Python AI service locally
bash
cd ai-service
pip install -r requirements.txt
python app.py
The service will start at http://localhost:5001.
Verify it works:

bash
curl -X POST http://localhost:5001/analyze -H "Content-Type: application/json" -d '{"text":"I love this!"}'
Expected output: {"sentiment":"positive","confidence":0.99,.....}

4. Build and run the Java app locally
Option A: Using Tomcat (manual)
Build the WAR:

bash
mvn clean package
Rename the generated WAR (found in target/) to ROOT.war and copy it to Tomcat’s webapps/ folder.

Start Tomcat

The app will be available at http://localhost:8080.

Option B: Using Docker (easier)
Build the Docker image:

bash
docker build -t sentiment-java .
Run the container with environment variables pointing to your local AI service and database:

bash
docker run -p 8080:8080 \
  -e MYSQL_URL="jdbc:mysql://host.docker.internal:3306/sentiment_dashboard?useSSL=false&serverTimezone=UTC" \
  -e MYSQL_USER="root" \
  -e MYSQL_PASSWORD="yourpassword" \
  -e AI_SERVICE_URL="http://host.docker.internal:5001/analyze" \
  sentiment-java
Access the app at http://localhost:8080/register.

Note: host.docker.internal allows the container to connect to services running on your host machine. Adjust the database URL if your MySQL is elsewhere.

Deployment
Deploy the Python AI Service on Hugging Face Spaces
Create a new Space at huggingface.co/new-space.

Choose Docker as the SDK.

Name it relevant to ai sentiment intelligence

Set the Space to Public.

Upload the following files to the Space’s repository:

ai-service/app.py

ai-service/requirements.txt

A Dockerfile with the content below:

dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
ENV PORT=7860
CMD ["python", "app.py"]
The Space will automatically build and run. Once it’s running, you’ll get a URL like https://jainam-mehta-ai-sentiment-service.hf.space.
Your AI service endpoint will be https://......./analyze.

Deploy the Java App on Render
Prepare the Docker image
Ensure your repository contains:

Dockerfile (provided in the repo)

ROOT.war (build it with mvn clean package and copy/rename to ROOT.war)

Commit and push these files to GitHub.

Create a Web Service on Render

Log in to Render.

Click New + → Web Service.

Connect your GitHub repository.

Environment: Docker (Render will detect the Dockerfile).

Name: e.g., sentiment-java

Region: Choose a region close to your users.

Instance Type: Free (512 MB RAM).

Add environment variables (see table below).
Use the values from your deployed AI service and your MySQL database

Click Create Web Service.
Render will build the Docker image and start the container.

Once deployed, your app will be available at https://your-app-name.onrender.com.

Environment Variables Reference
All environment variables are read by the Java application. Set them in your Render dashboard, or in Docker locally.

Variable	Description	Example

MYSQL_URL	JDBC URL of your MySQL database	jdbc:mysql://mysql-instance:3306/sentiment_dashboard?useSSL=false&serverTimezone=UTC
MYSQL_USER	Database user	root
MYSQL_PASSWORD	Database password	secret
AI_SERVICE_URL	Full URL of the AI service (including /analyze)	https://jainam-mehta-ai-sentiment-service.hf.space/analyze
EMAIL_USERNAME	Gmail address for sending reports	your-email@gmail.com
EMAIL_PASSWORD	Gmail App Password (not regular password)	abcd efgh ijkl mnop
Important: For Gmail, you must use an App Password (enable 2FA first). The regular password will not work.

Testing the Deployed Services
AI service health check:
https://your-space.hf.space/health → should return {"status":"healthy"}.

Java app registration:
https://your-app.onrender.com/register → you should see the registration page.

Start a stream:
Log in, enter a keyword (e.g., “mustang”), choose “Simulated”, and click Start Monitoring. Posts will appear with sentiment.

Test email reports:
After logging in, visit /sendNow (e.g., https://your-app.onrender.com/sendNow) to trigger an immediate daily report. Check your inbox (and spam folder).

Troubleshooting

Database connection refused
Verify the MYSQL_URL uses the correct host. If the database is on a remote server, ensure it allows remote connections.

If you’re using a local MySQL for development, use host.docker.internal when running the Java app in Docker.

AI service returns 404
Ensure AI_SERVICE_URL includes /analyze (e.g., https://......./analyze).

Check the Hugging Face Space logs for errors. The Space might have crashed.

Email not sent
Use a Gmail App Password, not your regular password.

Check the logs for AuthenticationFailedException or “Email credentials not set”.

Try /sendNow to force a report and see the exact error.

Out of memory on Render free tier
The free tier has 512 MB RAM. If the app exceeds this, it may be killed. You can limit Tomcat’s heap by adding the environment variable:

text
JAVA_OPTS=-Xmx256m
This reduces memory usage.

Reddit stream not working
The Reddit API may rate‑limit unauthenticated requests. The code uses a public endpoint without authentication, which is fine for low volume. If you need higher limits, consider adding a Reddit API key.

Project Structure
text
ai-sentiment-intelligence/
├── src/                         # Java source code (servlets, DAOs, ...., etc.)
├── ai-service/                  # Python AI service
│   ├── app.py
│   └── requirements.txt
├── MySQL_WorkBench_Files/       # SQL scripts
├── Dockerfile                   # For building the Java app container
├── ROOT.war                     # (generated) Deployable WAR file
├── pom.xml                      # Maven configuration
└── README.md

Contributing
Contributions are welcome! Feel free to open issues or submit pull requests.

License
This project is licensed under the MIT License.

Acknowledgements
Hugging Face for the pre‑trained model and Spaces hosting

Render for the free tier

Clever Cloud for the MySQL add‑on

The open‑source community for all the libraries used

IMP NOTE:
EMAIL ERROR MIGHT OCCUR:
The email error is a connection timeout to smtp.gmail.com:587. This typically happens because Render’s free tier may block outbound SMTP to prevent spam. The credentials are correct, but the container cannot reach Gmail’s SMTP server.
