<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\User;
use Error;
use Illuminate\Support\Facades\Hash;
use Symfony\Contracts\Service\Attribute\Required;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;


class UerController extends Controller
{
    public function index(Request $request){
        return $request->user();
    }
    public function register(Request $request){
    $request->validate([
            'name' => 'required',
            'email' => 'required|unique:users',
            'password' => 'required|confirmed'
        ]);
        $email = $request->email;
        $select = DB::table('users')
        ->select('email')
        ->where('email',$email)
        ->count();

if($select > 0 ){
$result = response()->json([
                'message' => 'Failed',
                'meta' => "Email already exists!" ]);
            return $result;
}
        else {
            $query = User::Create([
                'name' => $request->name,
                'email' => $request->email,
                'password' => Bcrypt($request->password)
            ]);

            if ($query) {
                $result = response()->json([
                    'message' => 'Success'

                ]);
                return $result;

            }
            else {
                $result = response()->json([
                    'message' => 'Fail',
                    'meta'=>'We all have bad days :( Try registereing again!'
                ]);
                return $result;
            }
        }
    }

    public function login(Request $request){
        $credentials  = $request->validate([
            'email'=>'required|email',
            'password'=>'required',
        ]);

        if(Auth::attempt($credentials)){
            $user = Auth::user();
            $token = md5(time()) . '.' . md5($request->email);
            $request->user()->forceFill([
                'api_token' => $token,
            ])->save();
            
            $result = response()->json([
                'message'=>"Valid",
                'tag'=>"V",
                'token' => $token,
                'user_id' =>$user->id,
                'email'=>$user->email
               
            ]);
            return $result;

        } else{
            return response()->json([
                'tag'=>'I',
                'message'=> "invalid credentials"
            ]);
        }
      
    }

    public function logout(Request $request){
        $request->user()->forceFill([
            'api_token' => null,
        ])->save();
        return response()->json([
            'message' => "Success"
        ]);
    }
}
