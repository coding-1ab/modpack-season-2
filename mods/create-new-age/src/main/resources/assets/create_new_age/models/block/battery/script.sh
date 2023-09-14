for i in {0..14}
  do
    echo "{
    \"parent\": \"create_new_age:block/battery/battery_controller\"
\"textures\": {
		\"0\": \"create_new_age:block/battery/screen/screen-$i\",
		\"1\": \"create_new_age:block/battery/battery\",
		\"particle\": \"create_new_age:block/battery/battery\"
	},
}" > "charge/$i.json"
done