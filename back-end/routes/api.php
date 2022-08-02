<?php

use App\Http\Controllers\ProjectController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| is assigned the "api" middleware group. Enjoy building your API!
|
*/
Route::post('login','UerController@login');
Route::post('register','UerController@register');


Route::middleware('auth:api')->group(function(){
    Route::get('user','UerController@index');
    Route::post('logout','UerController@logout');
    Route::get('projects','ProjectController@list');
    Route::get('myProjects', 'ProjectController@UserProjects');

});

Route::post('savePoints', 'ProjectController@SaveCoordinates');
Route::post('points','ProjectController@getPoints');
Route::post('create_project', 'ProjectController@create');
Route::post('savebasepoints', 'ProjectController@saveBasePoints');
Route::post('getBasepoints', 'ProjectController@getBasepoints');
Route::post('deleteProject', 'ProjectController@delete');
Route::post('deleteCoords', 'ProjectController@deleteCoords');



//Route::resource('projects', ProjectsController::class);


