"""Train the Random Forest model from the included synthetic demonstration dataset."""
import os, json
import pandas as pd
import numpy as np
import joblib
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

ROOT = os.path.dirname(os.path.abspath(__file__))
df = pd.read_csv(os.path.join(ROOT, "data.csv"))
X = pd.get_dummies(df.drop(columns=["braking_distance_m"]), columns=[
    "road_condition", "tyre_condition", "vehicle_load", "road_slope"
])
y = df["braking_distance_m"]
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
model = RandomForestRegressor(n_estimators=120, random_state=42, min_samples_leaf=2, n_jobs=-1)
model.fit(X_train, y_train)
pred = model.predict(X_test)
bundle = {"model": model, "columns": list(X.columns)}
os.makedirs(os.path.join(ROOT, "model"), exist_ok=True)
joblib.dump(bundle, os.path.join(ROOT, "model", "braking_model.joblib"))
metrics = {
    "dataset_rows": len(df), "train_rows": len(X_train), "test_rows": len(X_test),
    "MAE_m": round(float(mean_absolute_error(y_test, pred)), 3),
    "RMSE_m": round(float(np.sqrt(mean_squared_error(y_test, pred))), 3),
    "R2": round(float(r2_score(y_test, pred)), 4),
    "note": "These scores compare predictions with synthetic labels, not real vehicle test measurements."
}
os.makedirs(os.path.join(ROOT, "results"), exist_ok=True)
with open(os.path.join(ROOT, "results", "metrics.json"), "w") as f:
    json.dump(metrics, f, indent=2)
print(json.dumps(metrics, indent=2))
