from airflow import DAG
from airflow.operators.bash_operator import BashOperator
from datetime import datetime, timedelta
from airflow.utils.email import send_email

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2018, 11, 13),
    'email': ['gchasifa@thoughtworks.com', 'sleblanc@thoughtworks.com'],
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 0,
    'retry_delay': timedelta(minutes=5),
}

dag = DAG('test_file_checker_dag', default_args=default_args, schedule_interval='*/10 * * * *')

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

    send_email('gchasifa@thoughtworks.com, sleblanc@thoughtworks.com, cpatel@thoughtworks.com', title, body)

file_check_task = """
export AWS_DEFAULT_REGION=us-east-2
step=$(aws emr add-steps --cluster-id j-23A3N2OTQ6ZES --steps Type=SPARK,Name="Test File Check",ActionOnFailure=CONTINUE,Args=[--queue,monitoring,--class,com.free2wheelers.apps.FileChecker,/tmp/free2wheelers-file-checker_2.11-0.0.1.jar,/free2wheelers/stationMart/data] | python -c 'import json,sys;obj=json.load(sys.stdin);print obj.get("StepIds")[0];')
echo '========='$step
aws emr wait step-complete --cluster-id j-23A3N2OTQ6ZES --step-id $step
"""

o1 = BashOperator(
    task_id='file_check_task',
    bash_command=file_check_task,
    on_failure_callback=notify_email,
    dag=dag)
