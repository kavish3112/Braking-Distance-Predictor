AI-BASED VEHICLE BRAKING DISTANCE PREDICTOR — WEB APPLICATION
============================================================

WHAT THIS IS
A Flask web application. You enter vehicle speed and choose road, tyre, load,
and slope conditions. A trained Random Forest Regression model estimates the
braking distance and displays it in your browser.

IMPORTANT PROJECT LIMITATION
The 15,000-row dataset and trained model use synthetic demonstration data made
from a simplified illustrative formula. They are not based on verified real
vehicle braking tests. The prediction and evaluation scores are for learning
only and must not be used for driving decisions, vehicle design, or safety.
Braking distance means the distance travelled after the brakes are applied
until the vehicle stops; driver reaction distance is not included.

RUN LOCALLY ON WINDOWS
1. Install Python 3.11 (or another compatible Python 3 version).
2. Extract this ZIP file.
3. Open Command Prompt in the extracted folder.
4. Run these commands one at a time:
   py -m venv .venv
   .venv\Scripts\activate
   pip install -r requirements.txt
   python app.py
5. Open this address in your browser:
   http://127.0.0.1:5000
This is a local address; only your computer can access it by default.

DEPLOY TO THE INTERNET USING RENDER (PUBLIC LINK)
You must use your own hosting account. These files do not create a public link
until you upload the project and deploy it. The exact Render screens can change.

1. Sign in to GitHub and create a NEW repository, for example:
   ai-braking-distance-predictor
2. Upload all files and folders from this project to the repository. Include
   app.py, Procfile, .python-version, requirements.txt, templates/, static/,
   model/, data.csv, train_model.py, and results/.
   Keep model/braking_model.joblib in the repository; the web app needs it.
3. Sign in to Render and choose to create a new Web Service from your GitHub
   repository. Connect the repository you just created.
4. Set the build command to:
   pip install -r requirements.txt
5. Set the start command to:
   gunicorn app:app
6. Choose a Python-compatible web service environment and click Deploy.
7. Wait for the deployment to finish. Render will show a public URL similar to
   https://your-service-name.onrender.com. Use the actual URL Render gives you.
8. Open that URL and test a few inputs. Do not share secrets or passwords in
   the repository. The project contains no API keys.

IF DEPLOYMENT FAILS
- Confirm model/braking_model.joblib was uploaded and is not missing.
- Confirm requirements.txt and app.py are at the repository root.
- Check the deployment logs for the first error message.
- If Python version setup causes an issue, set the service Python version to a
  version supported by Render and compatible with the listed packages.

FILES
- app.py: Flask web application and prediction route
- templates/index.html: webpage form and prediction display
- static/style.css: page styling
- model/braking_model.joblib: trained Random Forest model
- data.csv: 15,000 synthetic demonstration rows
- train_model.py: training script
- results/: sample predictions and evaluation information
- Procfile: production start command
- requirements.txt: Python dependencies
