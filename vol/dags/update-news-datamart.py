from airflow import DAG
from airflow.operators.dummy_operator import DummyOperator
from airflow.operators.python_operator import PythonOperator
from airflow.operators.python import BranchPythonOperator
from airflow.operators.bash_operator import BashOperator
from airflow.decorators import task

from datetime import datetime
from random import randint
import os.path
from os import system

with DAG('update-news-datamart', description='update-news-datamart', 
    start_date=datetime(2022, 6, 12), schedule_interval='30 0 * * *', catchup=False) as dag:    

    all_task_oper = BashOperator(
        task_id="all_task",
        bash_command="java -cp \"./*\" main.Main all",
        cwd = "/app"
    )

    prep_task_oper = BashOperator(
        task_id="prep_task",
        bash_command="java -cp \"./*\" main.Main prep",
        cwd = "/app"
    )

    uncat_task_oper = BashOperator(
        task_id="uncat_task",
        bash_command="java -cp \"./*\" main.Main uncat",
        cwd = "/app"
    )

    anl_task_oper = BashOperator(
        task_id="anl_task",
        bash_command="java -cp \"./*\" main.Main anl",
        cwd = "/app"
    )


    all_task_oper >> prep_task_oper >> uncat_task_oper >> anl_task_oper

