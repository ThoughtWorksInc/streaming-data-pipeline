from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta
from airflow.utils.email import send_email
from airflow.models import Variable


default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime.today().strftime('%Y-%m-%d'),
    'email': ['gchasifa@thoughtworks.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 0,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('monitoring_file_checker_v1', default_args=default_args, schedule_interval='*/10 * * * *')

def notify_email(contextDict, **kwargs):
    """Send custom email alerts."""

    # email title.
    title = "Something went wrong with stationMart in HDFS".format(**contextDict)

    # email contents
    body = """
    Hi Everyone, <br>
    <br>
    There's been an error with stationMart in HDFS<br>
    <br>
    """.format(**contextDict)

    send_email('TWDU-June2019-Participants@thoughtworks.com', title, body)

file_check_task = """
export AWS_DEFAULT_REGION=us-east-2
step=$(aws emr add-steps --cluster-id {{ var.json.prod.cluster-id }} --steps Type=SPARK,Name="Test File Check",ActionOnFailure=CONTINUE,Args=[--class,com.free2wheelers.apps.FileChecker,--master,yarn,--deploy-mode,cluster,--queue,monitoring,/tmp/free2wheelers-file-checker_2.11-0.0.1.jar,/free2wheelers/stationMart/data] | python -c 'import json,sys;obj=json.load(sys.stdin);print obj.get("StepIds")[0];')
echo '========='$step
aws emr wait step-complete --cluster-id {{ var.json.prod.cluster-id }} --step-id $step
"""

o1 = BashOperator(
    task_id='file_check_task',
    bash_command=file_check_task,
    on_failure_callback=notify_email,
    dag=dag)
