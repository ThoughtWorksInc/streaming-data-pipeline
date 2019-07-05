import os
from datetime import datetime, timedelta

from airflow import DAG
from airflow import LoggingMixin
from airflow.contrib.operators.emr_add_steps_operator import EmrAddStepsOperator
from airflow.contrib.sensors.emr_step_sensor import EmrStepSensor
from airflow.models import Variable

logger = LoggingMixin()

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime.today().strftime('%Y-%m-%d'),
    'retries': 0,
    'retry_delay': timedelta(minutes=5),
}


def handle_task_failure(contextDict, **kwargs):
    logger.log.info("op=handle_task_failure | Handling the failure")

    env_variables = Variable.get("monitoring_file_checker_v1", deserialize_json=True)
    cohort = env_variables["cohort"]
    aws_region = env_variables["aws_region"]

    bash_command = """
    export AWS_DEFAULT_REGION={aws_region}
    aws cloudwatch put-metric-data --namespace streaming-data-pipeline-{cohort} --metric-name output-file-validation-failed --value 1""".format(
        cohort=cohort, aws_region=aws_region)
    os.system(bash_command)

    logger.log.info("op=handle_task_failure | Published metrics successfully")


def handle_task_success(contextDict, **kwargs):
    logger.log.info("op=handle_task_success | Handling the success")

    env_variables = Variable.get("monitoring_file_checker_v1", deserialize_json=True)
    cohort = env_variables["cohort"]
    aws_region = env_variables["aws_region"]

    bash_command = """
    export AWS_DEFAULT_REGION={aws_region}
    aws cloudwatch put-metric-data --namespace streaming-data-pipeline-{cohort} --metric-name output-file-validation-failed --value 0""".format(
        cohort=cohort, aws_region=aws_region)
    os.system(bash_command)

    logger.log.info("op=handle_task_success | Published metrics successfully")


SPARK_STEPS = [
    {
        'Name': 'File Check',
        'ActionOnFailure': 'CONTINUE',
        'HadoopJarStep': {
            'Jar': 'command-runner.jar',
            'Args': [
                'spark-submit',
                '--master',
                'yarn',
                '--deploy-mode',
                'cluster',
                '--class',
                'com.free2wheelers.apps.FileChecker',
                '--queue',
                'default',
                '/usr/lib/citibike-apps/free2wheelers-file-checker_2.11-0.0.1.jar',
                'hdfs:///free2wheelers/stationMart/data'
            ]
        }
    }
]

dag = DAG('monitoring_file_checker_v1', default_args=default_args, schedule_interval='*/5 * * * *', catchup=False)

add_steps = EmrAddStepsOperator(
    task_id='add_steps',
    job_flow_id='{{ var.json.monitoring_file_checker_v1.cluster_id }}',
    aws_conn_id='aws_default',
    steps=SPARK_STEPS,
    dag=dag)

watch_step = EmrStepSensor(
    task_id='watch_step',
    job_flow_id="{{ var.json.monitoring_file_checker_v1.cluster_id }}",
    step_id="{{ task_instance.xcom_pull('add_steps', key='return_value')[0] }}",
    aws_conn_id='aws_default',
    on_failure_callback=handle_task_failure,
    on_success_callback=handle_task_success,
    dag=dag
)

add_steps >> watch_step
