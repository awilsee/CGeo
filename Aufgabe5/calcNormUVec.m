function [ n ] = calcNormUVec( a, b, direction )
    v = b - a; % calculating direction vector
    
    % calculate normal unit vector in dependence of direction
    if( direction > 0 )
        n = [ -v(2); v(1) ];
    else
        n = [ v(2); -v(1) ];
    end
    
    n = n ./ norm( n, 2 ); % normalize vector
end