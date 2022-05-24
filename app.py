# Run this app with `python app.py` and
# visit http://127.0.0.1:8050/ in your web browser.

from dash import Dash, html, dcc, dash_table, Input, Output
import plotly.express as px
import plotly.graph_objects as go
import pandas as pd
from pinotdb import connect

# external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']
# app = Dash(__name__, external_stylesheets=external_stylesheets)
app = Dash(__name__)
app.title = "Collaborative pixel canvas"

def as_data_table_or_message(df, message):
    return as_datatable(df) if df.shape[0] > 0 else message

def as_datatable(df):
    return dash_table.DataTable(
        df.to_dict('records'), [{"name": i, "id": i} for i in df.columns]
    )

def add_delta_trace(fig, title, value, last_value, row, column):
    fig.add_trace(go.Indicator(
        mode = "number+delta",
        title= {'text': title},
        value = value,
        delta = {'reference': last_value, 'relative': True},
        domain = {'row': row, 'column': column})
    )

def add_trace(fig, title, value, row, column):
    fig.add_trace(go.Indicator(
        mode = "number",
        title= {'text': title},
        value = value,
        domain = {'row': row, 'column': column})
    )

connection = connect(host="localhost", port="8099", path="/query/sql", scheme=( "http"))
cursor = connection.cursor()

cursor.execute("""
    SELECT user, COUNT(*) AS counter FROM pixelEvent GROUP BY user ORDER BY counter desc
    """, {"intervalString": f"PT30M"})

pairs_df = pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])
pairs = as_data_table_or_message(pairs_df, "No updates")

cursor.execute("""
SELECT count(*) AS count FROM pixelEvent""", {"intervalString": f"PT30M"})
pixel_updated_count =  pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])

cursor.execute("""
SELECT count(*) AS count FROM pixelEvent WHERE "time" > ago(%(intervalString)s)""", {"intervalString": f"PT5m"})
activity =  pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])

fig = go.Figure(layout=go.Layout(height=300))
add_trace(fig, "Total updated", pixel_updated_count["count"][0], 0, 0)
add_trace(fig, "Activity [5m]", activity["count"][0], 0, 1)
fig.update_layout(grid = {"rows": 1, "columns": 2,  'pattern': "independent"},)

cursor.close()


app.layout = html.Div(children=[
    html.H1(children='Collaborative pixel canvas', style={'text-align': 'center'}),
    dcc.Interval(
            id='interval-component',
            interval=1 * 1000,
            n_intervals=0
    ),

    html.Div(id='content', children=[
        html.H2("Activity"), dcc.Graph(id="stat", figure=fig),
        html.H2("Most active user"), html.Div(
        id='datatable',
        children=[pairs]
        )
    ])


])


@app.callback(
    Output(component_id='datatable', component_property='children'),
    Output(component_id='stat', component_property='figure'),
    Input('interval-component', 'n_intervals')
)
def update_output_div(input_value):
    cursor = connection.cursor()

    cursor.execute("""
    SELECT user, COUNT(*) AS counter FROM pixelEvent GROUP BY user ORDER BY counter desc
    """, {"intervalString": f"PT30M"})

    pairs_df = pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])
    pairs = as_data_table_or_message(pairs_df, "No updates")

    cursor.execute("""
    SELECT count(*) AS count FROM pixelEvent""", {"intervalString": f"PT30M"})
    pixel_updated_count =  pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])

    cursor.execute("""
    SELECT count(*) AS count FROM pixelEvent WHERE "time" > ago(%(intervalString)s)""", {"intervalString": f"PT5m"})
    activity =  pd.DataFrame(cursor, columns=[item[0] for item in cursor.description])

    fig = go.Figure(layout=go.Layout(height=300))
    add_trace(fig, "Total updated", pixel_updated_count["count"][0], 0, 0)
    add_trace(fig, "Activity [5m]", activity["count"][0], 0, 1)
    fig.update_layout(grid = {"rows": 1, "columns": 2,  'pattern': "independent"},)


    cursor.close()

    return pairs, fig


if __name__ == '__main__':
    app.run_server(debug=True)