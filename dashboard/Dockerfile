FROM python:3

RUN pip install jupyter-dash
RUN pip install pandas
RUN pip install pinotdb

ADD app.py /app/app.py

EXPOSE 8050

CMD ["python", "/app/app.py"]