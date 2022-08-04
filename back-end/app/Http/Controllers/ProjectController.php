<?php

namespace App\Http\Controllers;

use App\Models\Coordinates;
use Illuminate\Http\Request;
use App\Models\Project;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Auth;
use App\Models\Basepoints;

class ProjectController extends Controller
{
    // public function index(Request $request){
    //     Project::all();
    //     return $request->all();
    // }

    public function UserProjects(){

       if(Auth::check()){

        $userID = Auth::user()->id;
        $query = DB::table('projects')

        ->select('name','id','gap_size','mesh_size','user_id')
        ->orderBy('id','DESC')

        ->where('user_id',$userID)->get();

        $result = response()->json([
            'projects'=>$query,
            'message'=>'Success'
        ]);
        return $query;
       }

       else {
        $result = response()->json([
            'message'=>'Failed'
        ]);
            return $result;
       }


    }
    //NOT USED
    public function list(){
       $result =  Project::orderBy('id','DESC')->get();
        return $result;
    }

    public function getPoints(Request $request){
       // if(Auth::check()){

            $request->validate([
                'project_id' => 'required',
                'user_id'=>'required',

            ]);

        $userID = $request->user_id;
            $projectid = $request->project_id;
            $query = DB::table('coordinates')

        ->select('lat as Lat','lng as Long')

        ->where('user_id',$userID)
     ->where('project_id',$projectid)
     ->distinct()
        ->get();
if($query){
    $points = response()->json([
        'results'=>$query,
        'message'=>'Success'
    ]);
    return $points;
}else{
        $result = response()->json([
                'message' => 'Failed',

            ]);
            return $result;
}


        // }else{
        //     $result = response()->json([
        //         'message' => 'Failed',

        //     ]);
        //     return $result;
        // }

    }

    public function getBasepoints(Request $request){
        $request->validate([
            'project_id' => 'required',
        ]);
$projectID = $request->project_id;
        $query = DB::table('Basepoints')
            ->select('lat as Lat', 'lng as Long')
            ->where('project_id', $projectID)
            ->get();
            if($query){
            $result = response() -> json([
                'message' => 'Success',
                'points'=>$query
            ]);
            return $result;
            }
            else{
                $result = response() -> json([
                    'message' => 'Failed',
                    'meta'=>'Could not save'
                ]);
                return $result;
            }

    }
    public function create(Request $request)
    {
        $request->validate([
            'name' => 'required',
            'gap_size' => 'required',
            'user_id'=>'required',
            'mesh_size' =>'required'

        ]);
        $result = Project::Create([
            'name' =>$request->name,
            'gap_size'=>$request->gap_size,
            'user_id'=>$request->user_id,
            'mesh_size'=>$request->mesh_size,

        ]);

            $jsonResult = response()->json([
                'message'=>"created",
                'tag'=>"V",
                'name' => $result->name,
                'gap_size' =>$result->gap_size,
                'user_id'=>$result->user_id,
                'projectID'=>$result->id,
                'createdAt'=>$result->created_at,
                'mesh_size'=>$result->mesh_size,

            ]);

            return $jsonResult;
        }

        public function saveBasePoints(Request $request){
        $request->validate([
            'lat' => 'required',
            'lng' => 'required',
            'project_id' => 'required'
        ]);

        $query = Basepoints::Create([
            'lat' => $request->lat,
            'lng' => $request->lng,
            'project_id' => $request->project_id
        ]);
if($query){

        $result = response()->json([
            'message' => 'success',


        ]);
        return $result;
    }
     else{
        $result = response()->json([
            'message' => 'Failed',
            'meta'=>'could not save'

        ]);
        return $result;
    }
}

      public function SaveCoordinates(Request $request){
        // if (Auth::check()){
        //     $user_id = Auth::user()->id;

            $request->validate([
                'lat' => 'required',
                'lng' => 'required',
                'user_id'=>'required',
                'project_id'=>'required'

            ]);

            $result = Coordinates::Create([
                'lat' =>$request->lat,
                'lng'=>$request->lng,
                'project_id'=>$request->project_id,
                'user_id'=>$request->user_id

            ]);
            if($result){

                $result = response()->json([
                    'message' => 'success',


                ]);
                return $result;
            }
             else{
                $result = response()->json([
                    'message' => 'Failed',
                    'meta'=>'could not save'

                ]);
                return $result;
            }

        }

        public function delete(Request $request)
        {
         $request->validate([
            'project_id' => 'required'
         ]);

            $id = $request->project_id;
           $results = Project::find($id)->delete();
           if($results){
            return response()->json([
                'message' => 'success',
            ]);
           }else{
            return response()->json([
                'message' => 'failed',
            ]);
           }

        }

        public function deleteCoords(Request $request){
            $request->validate([
                'coordinates_id' => 'required'
             ]);

                $id = $request->coordinates_id;
               $results = Coordinates::find($id)->delete();
               if($results){
                return response()->json([
                    'message' => 'success',
                ]);
               }else{
                return response()->json([
                    'message' => 'failed',
                ]);
               }
        }

    }


