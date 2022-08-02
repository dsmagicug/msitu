<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Basepoints extends Model
{
    use HasFactory;

    protected $fillable = [
        'lat',
        'lng',
        'project_id',
      
    
        
    ];

}
