from django.conf.urls import url

from . import views
from .views import test_data

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^sceneViz/test_data', test_data, name='test_data'),
]